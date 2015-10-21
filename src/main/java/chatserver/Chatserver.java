package chatserver;

import executors.CommandBus;
import service.UserService;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Chatserver implements IChatserverCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

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
	public Chatserver(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		// TODO
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(12345);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DatagramSocket serverUdpSocket;
		try {
			serverUdpSocket = new DatagramSocket(12346);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		final UserService userService = new UserService();
		final CommandBus serverBus = new CommandBus();

		UdpHandler udpHandler = new UdpHandler(serverUdpSocket, userService);

		// add a local command executor
		//final StateMachine serverStateMachine = new StateMachine(new StateServerMain());
		//final CommandExecutor localExecutor = new LocalCommandExecutor(serverStateMachine);
		//serverBus.addCommandExecutor(localExecutor);

		while (true) {
			try {
				final Socket clientSocket = serverSocket.accept();
				new ClientHandler(clientSocket, userService);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String users() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		Chatserver chatserver = new Chatserver(args[0],
				new Config("chatserver"), System.in, System.out);
		// TODO: start the chatserver
	}

}
