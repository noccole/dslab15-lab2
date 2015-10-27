package client;

import channels.*;
import cli.Command;
import cli.Shell;
import commands.*;
import entities.PrivateAddress;
import entities.User;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	private void startTcpHandler() {
		Socket socket;
		try {
			socket = new Socket(config.getString("chatserver.host"), config.getInt("chatserver.tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Channel channel;
		try {
			channel = new MessageChannel(new Base64Channel(new TcpChannel(socket)));
		} catch (ChannelException e) {
			e.printStackTrace();
			return;
		}

		tcpRequester = new ClientHandler(channel, executorService);
		tcpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				lastPublicMessage = message;

				try {
					shell.writeLine(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onExit() {
				try {
					exit();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void startUdpHandler() {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.connect(InetAddress.getByName(config.getString("chatserver.host")), config.getInt("chatserver.udp.port"));
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return;
		}

		Channel channel;
		try {
			channel = new MessageChannel(new Base64Channel(new UdpChannel(socket)));
		} catch (ChannelException e) {
			e.printStackTrace();
			return;
		}

		udpRequester = new ClientHandler(channel, executorService);
		udpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				try {
					shell.writeLine(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onExit() {
				try {
					exit();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void run() {
		startUdpHandler();
		startTcpHandler();

		new Thread(shell).start();
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);

		try {
			LoginResponse response = tcpRequester.syncRequest(request);
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
	public String logout() throws IOException {
		LogoutRequest request = new LogoutRequest();

		try {
			LogoutResponse response = tcpRequester.syncRequest(request);
			this.username = null;
			return "Successfully logged out.";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		SendMessageRequest request = new SendMessageRequest();
		request.setMessage(message);

		try {
			SendMessageResponse response = tcpRequester.syncRequest(request);
			return "Successfully send message.";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String list() throws IOException {
		ListRequest request = new ListRequest();

		try {
			ListResponse response =  udpRequester.syncRequest(request);

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
	public String msg(String username, String message) throws IOException {
		final SendPrivateMessageRequest request = new SendPrivateMessageRequest();
		request.setSender(this.username);
		request.setMessage(message);

		// lookup user address
		final PrivateAddress privateAddress;
		{
			LookupRequest lookupRequest = new LookupRequest();
			lookupRequest.setUsername(username);

			try {
				LookupResponse response = tcpRequester.syncRequest(lookupRequest);
				privateAddress = response.getPrivateAddress();
			} catch (Exception e) {
				return "Wrong username or user not reachable.";
			}

			if (privateAddress == null) {
				return "Wrong username or user not reachable.";
			}
		}

		final Socket socket = new Socket(privateAddress.getHostname(), privateAddress.getPort());
		final Channel channel;
		try {
			channel = new MessageChannel(new Base64Channel(new TcpChannel(socket)));
		} catch (ChannelException e) {
			socket.close();
			return "Wrong username or user not reachable.";
		}

		final ClientHandler requester = new ClientHandler(channel, executorService);

		String result;
		try {
			SendPrivateMessageResponse response = requester.syncRequest(request);
			result = username + " replied with !ack.";
		} catch (Exception e) {
			result = e.getMessage();
		}

		requester.stop();

		return result;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		LookupRequest request = new LookupRequest();
		request.setUsername(username);

		try {
			LookupResponse response = tcpRequester.syncRequest(request);
			return String.valueOf(response.getPrivateAddress());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		final String[] addressParts = privateAddress.split(":");
		if (addressParts.length != 2) {
			return "Invalid private address.";
		}

		final PrivateAddress address = new PrivateAddress();
		address.setHostname(addressParts[0]);
		address.setPort(Integer.valueOf(addressParts[1]));

		final RegisterRequest request = new RegisterRequest();
		request.setPrivateAddress(address);

		try {
			RegisterResponse response = tcpRequester.syncRequest(request);
		} catch (Exception e) {
			return e.getMessage();
		}

		final ServerSocket serverSocket = new ServerSocket(address.getPort());
		PrivateMessageSocketListener listener = new PrivateMessageSocketListener(serverSocket, shell, executorService);
		executorService.submit(listener);

		return "Successfully registered address for " + username + ".";
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		return lastPublicMessage;
	}

	@Override
	@Command
	public String exit() throws IOException {
		logout();

		tcpRequester.stop();
		udpRequester.stop();

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
		final Config config = new Config("client");
		final Client client = new Client(args[0], config, System.in, System.out);

		new Thread(client).start();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
