package chatserver;

import channels.*;
import cli.Command;
import cli.Shell;
import entities.User;
import executors.EventDistributor;
import messages.ExitEvent;
import repositories.ConfigUserRepository;
import repositories.UserRepository;
import service.UserService;
import shared.HandlerBase;
import shared.HandlerFactory;
import shared.SocketConnectionListener;
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

public class Chatserver implements IChatserverCli, Runnable {
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Shell shell;
	private final Config config;

	private final UserService userService;
	private final EventDistributor eventDistributor;

	private ListHandler listHandler;

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

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);

		Config userConfig = new Config("user");
		UserRepository userRepository = new ConfigUserRepository(userConfig);
		userService = new UserService(userRepository);

		eventDistributor = new EventDistributor();
	}

	private boolean startListHandler() {
		final DatagramSocket serverUdpSocket;
		try {
			serverUdpSocket = new DatagramSocket(config.getInt("udp.port"));
		} catch (SocketException e) {
			System.err.println("could not open udp server socket");
			return false;
		}

		final Channel udpChannel;
		try {
			udpChannel = new MessageChannel(new Base64Channel(new UdpChannel(serverUdpSocket)));
		} catch (ChannelException e) {
			System.err.println("could not create a udp channel");
			return false;
		}

		listHandler = new ListHandler(udpChannel, userService, executorService);

		return true;
	}

	private boolean startServerSocketListener() {
		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			System.err.println("could not open tcp server socket");
			return false;
		}

		final SocketConnectionListener listener = new SocketConnectionListener(serverSocket, new HandlerFactory() {
			@Override
			public HandlerBase createHandler(Channel channel) {
				return new ClientHandler(channel, userService, eventDistributor, executorService);
			}
		});
		executorService.submit(listener);

		return true;
	}

	@Override
	public void run() {
		executorService.submit(shell);

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
		// inform all clients
		eventDistributor.publish(new ExitEvent());

		if (listHandler != null) {
			listHandler.stop();
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("could not shutdown executor service, force it ...");
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
