package chatserver;

import channels.*;
import cli.Command;
import cli.Shell;
import entities.User;
import messages.ExitEvent;
import messages.UserPresenceChangedEvent;
import repositories.ConfigUserRepository;
import repositories.UserRepository;
import service.UserService;
import shared.*;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Chatserver implements IChatserverCli, Runnable {
	private static final Logger LOGGER = Logger.getAnonymousLogger();

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Shell shell;
	private final Config config;

	private final UserService userService;
	private final EventDistributor eventDistributor;

	private SocketConnectionListener socketListener;

	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public Chatserver(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.config = config;

		LogManager.getLogManager().reset(); // disable logging

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);

		eventDistributor = new EventDistributor();

		Config userConfig = new Config("user");
		UserRepository userRepository = new ConfigUserRepository(userConfig);
		userService = new UserService(userRepository);
	}

	private boolean startListHandler() {
		final DatagramSocket serverUdpSocket;
		try {
			serverUdpSocket = new DatagramSocket(config.getInt("udp.port"));
		} catch (SocketException e) {
			LOGGER.warning("could not open udp server socket");
			return false;
		}

		final Channel udpChannel;
		try {
			udpChannel = new MessageChannel(new Base64Channel(new UdpChannel(serverUdpSocket)));
		} catch (ChannelException e) {
			LOGGER.warning("could not create a udp channel");
			return false;
		}

		new ListHandler(udpChannel, userService, executorService);

		return true;
	}

	private boolean startServerSocketListener() {
		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			LOGGER.warning("could not open tcp server socket");
			return false;
		}

		socketListener = new SocketConnectionListener(serverSocket, new HandlerFactory() {
			@Override
			public HandlerBase createHandler(Channel channel) {
				return new ClientHandler(channel, userService, eventDistributor, executorService);
			}
		});
		executorService.submit(socketListener);

		return true;
	}

	@Override
	public void run() {
		new Thread(shell).start();

		if (!startListHandler()) {
			exit();
		}
		if (!startServerSocketListener()) {
			exit();
		}
	}

	@Override
	@Command
	public String users() {
		final Map<String, User.Presence> userList = userService.getUserList();

		String result = "";
		for (Map.Entry<String, User.Presence> entry : userList.entrySet()) {
			final String username = entry.getKey();
			final User.Presence presence = entry.getValue();

			result += username + (presence == User.Presence.Offline ? " offline" : " online") + "\n";
		}

		return result;
	}

	@Override
	@Command
	public String exit() {
		executorService.shutdown();

		// inform all clients
		//eventDistributor.publish(new ExitEvent()); not needed in the assignment
		//eventDistributor.waitForAllMessagesSend();

		if (socketListener != null) {
			socketListener.cancel(true);
		}

		HandlerManager.getInstance().stopAllHandlers();

		try {
			if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			LOGGER.warning("Could not shutdown executor service, force it ...");
			executorService.shutdownNow();
		}

		shell.close();

		return "Shut down completed! Bye ..";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		final Config config = new Config("chatserver");
		final Chatserver chatserver = new Chatserver(args[0], config, System.in, System.out);

		chatserver.run();
	}

}
