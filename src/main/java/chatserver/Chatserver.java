package chatserver;

import cli.Command;
import cli.Shell;
import entities.User;
import repositories.ConfigUserRepository;
import repositories.UserRepository;
import service.UserService;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Map;

public class Chatserver implements IChatserverCli, Runnable {
	private final Shell shell;
	private final Config config;

	private final UserService userService;

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
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DatagramSocket serverUdpSocket;
		try {
			serverUdpSocket = new DatagramSocket(config.getInt("udp.port"));
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		new UdpHandler(serverUdpSocket, userService);
		new Thread(new TcpHandler(serverSocket, userService)).start();

		new Thread(shell).start();
	}

	@Override
	@Command
	public String users() throws IOException {
		final Map<String, User.Presence> userList = userService.getUserList();

		String result = "Online users:";
		for (Map.Entry<String, User.Presence> entry : userList.entrySet()) {
			final String username = entry.getKey();
			final User.Presence presence = entry.getValue();

			if (presence == User.Presence.Available) {
				result += "\n" + username;
			}
		}

		return result;
	}

	@Override
	@Command
	public String exit() throws IOException {
		// TODO close all resources

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

		new Thread(chatserver).start();
	}

}
