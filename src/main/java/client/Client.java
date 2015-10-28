package client;

import channels.*;
import cli.Command;
import cli.Shell;
import entities.PrivateAddress;
import entities.User;
import messages.*;
import shared.HandlerBase;
import shared.HandlerFactory;
import shared.SocketConnectionListener;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Shell shell;
	private final Config config;

	private String username;
	private String lastPublicMessage = "No message received !";

	private ClientHandler tcpRequester;
	private ClientHandler udpRequester;

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
	public Client(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.config = config;

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	private boolean startTcpHandler() {
		final Socket socket;
		try {
			socket = new Socket(config.getString("chatserver.host"), config.getInt("chatserver.tcp.port"));
		} catch (IOException e) {
			System.err.println("could not open tcp socket");
			return false;
		}

		final Channel channel;
		try {
			channel = new MessageChannel(new Base64Channel(new TcpChannel(socket)));
		} catch (ChannelException e) {
			System.err.println("could not create tcp channel");
			return false;
		}

		tcpRequester = new ClientHandler(channel, executorService);
		tcpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				lastPublicMessage = message;

				try {
					shell.writeLine(message);
				} catch (IOException e) {
					System.err.println("could not write message");
				}
			}

			@Override
			public void onExit() {
				exit();
			}
		});

		return true;
	}

	private boolean startUdpHandler() {
		final DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.connect(InetAddress.getByName(config.getString("chatserver.host")), config.getInt("chatserver.udp.port"));
		} catch (UnknownHostException | SocketException e) {
			System.err.println("could not open udp socket");
			return false;
		}

		final Channel channel;
		try {
			channel = new MessageChannel(new Base64Channel(new UdpChannel(socket)));
		} catch (ChannelException e) {
			System.err.println("could not create udp channel");
			return false;
		}

		udpRequester = new ClientHandler(channel, executorService);
		udpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				try {
					shell.writeLine(message);
				} catch (IOException e) {
					System.err.println("could not write message");
				}
			}

			@Override
			public void onExit() {
				exit();
			}
		});

		return true;
	}

	@Override
	public void run() {
		executorService.submit(shell);

		if (!startUdpHandler()) {
			exit();
		}
		if (!startTcpHandler()) {
			exit();
		}
	}

	@Override
	@Command
	public String login(String username, String password) {
		final LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);

		try {
			final LoginResponse response = tcpRequester.syncRequest(request);
			if (response.isSuccess()) {
				this.username = username;
				return "Successfully logged in.";
			} else {
				return "Wrong username or password.";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String logout() {
		final LogoutRequest request = new LogoutRequest();

		try {
			final LogoutResponse response = tcpRequester.syncRequest(request);
			this.username = null;
			return "Successfully logged out.";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String send(String message) {
		final SendMessageRequest request = new SendMessageRequest();
		request.setMessage(message);

		try {
			final SendMessageResponse response = tcpRequester.syncRequest(request);
			return "Successfully send message.";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String list() {
		final ListRequest request = new ListRequest();

		try {
			final ListResponse response =  udpRequester.syncRequest(request);

			String result = "Online users:";
			for (Map.Entry<String, User.Presence> entry : response.getUserList().entrySet()) {
				final String username = entry.getKey();
				final User.Presence presence = entry.getValue();

				if (presence == User.Presence.Available) {
					result += "\n" + username;
				}
			}

			return result;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String msg(String username, String message) {
		final SendPrivateMessageRequest request = new SendPrivateMessageRequest();
		request.setSender(this.username);
		request.setMessage(message);

		// lookup user address
		final Collection<PrivateAddress> privateAddresses;
		{
			final LookupRequest lookupRequest = new LookupRequest();
			lookupRequest.setUsername(username);

			try {
				final LookupResponse response = tcpRequester.syncRequest(lookupRequest);
				privateAddresses = response.getPrivateAddresses();
			} catch (Exception e) {
				return "Wrong username or user not reachable.";
			}
		}

		for (PrivateAddress privateAddress : privateAddresses) {
			final Socket socket;
			try {
				socket = new Socket(privateAddress.getHostname(), privateAddress.getPort());
			} catch (IOException e) {
				System.err.println("could not open private message socket: " + e);
				continue; // try next address
			}

			final Channel channel;
			try {
				channel = new MessageChannel(new Base64Channel(new TcpChannel(socket)));
			} catch (ChannelException e) {
				System.err.println("could not create private message channel: " + e);
				try {
					socket.close();
				} catch (IOException e1) {
					System.err.println("could not close private message socket: " + e1);
				}
				continue; // try next address
			}

			final ClientHandler requester = new ClientHandler(channel, executorService);
			try {
				SendPrivateMessageResponse response = requester.syncRequest(request);
				requester.stop();
				return username + " replied with !ack."; // success
			} catch (Exception e) {
				requester.stop();
				continue; // try next address
			}
		}

		return "Wrong username or user not reachable.";
	}

	@Override
	@Command
	public String lookup(String username) {
		final LookupRequest request = new LookupRequest();
		request.setUsername(username);

		try {
			final LookupResponse response = tcpRequester.syncRequest(request);

			String result = "";
			for (PrivateAddress privateAddress : response.getPrivateAddresses()) {
				result += String.valueOf(privateAddress) + "\n";
			}
			return result;
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	@Command
	public String register(String privateAddress) {
		final String[] addressParts = privateAddress.split(":");
		if (addressParts.length != 2) {
			return "Invalid private address.";
		}

		final PrivateAddress address = new PrivateAddress();
		address.setHostname(addressParts[0]);
		address.setPort(Integer.valueOf(addressParts[1]));

		// try to open the server socket, if it succeeds then register the private address
		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(address.getPort());
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		} catch (IOException e) {
			return "Could not open private message server socket.";
		}

		final RegisterRequest request = new RegisterRequest();
		request.setPrivateAddress(address);

		try {
			final RegisterResponse response = tcpRequester.syncRequest(request);
		} catch (Exception e) {
			try {
				serverSocket.close();
			} catch (IOException e1) {
				System.err.println("Could not close private message server socket");
			}
			return e.getMessage();
		}

		final SocketConnectionListener listener = new SocketConnectionListener(serverSocket, new HandlerFactory() {
			@Override
			public HandlerBase createHandler(Channel channel) {
				return new PrivateMessageHandler(channel, executorService);
			}
		});
		executorService.submit(listener);

		return "Successfully registered address for " + username + ".";
	}
	
	@Override
	@Command
	public String lastMsg() {
		return lastPublicMessage;
	}

	@Override
	@Command
	public String exit() {
		logout();

		if (tcpRequester != null) {
			tcpRequester.stop();
		}
		if (udpRequester != null) {
			udpRequester.stop();
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
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		final Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		final Logger logger = Logger.getAnonymousLogger();
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		final Config config = new Config("client");
		final Client client = new Client(args[0], config, System.in, System.out);

		client.run();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
