package client;

import channels.*;
import cli.Command;
import cli.Shell;
import commands.*;
import entities.PrivateAddress;
import entities.User;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Client implements IClientCli, Runnable {
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Shell shell;
	private final Config config;

	private String username;

	private MessageListener messageListener;
	private MessageSender messageSender;

	private MessageListener udpMessageListener;
	private MessageSender udpMessageSender;

	private ExecutorService executor = Executors.newCachedThreadPool();

	private final ClientMainState state = new ClientMainState();

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

	@Override
	public void run() {
		Socket socket;
		try {
			socket = new Socket(config.getString("chatserver.host"), config.getInt("chatserver.tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DatagramSocket udpSocket;
		try {
			udpSocket = new DatagramSocket();
			udpSocket.connect(InetAddress.getByName(config.getString("chatserver.host")), config.getInt("chatserver.udp.port"));
		} catch (UnknownHostException | SocketException e) {
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

		messageListener = new ChannelMessageListener(channel);
		messageSender = new ChannelMessageSender(channel);

		StateMachine stateMachine = new StateMachine(state);
		final MessageHandler messageHandler = new StateMachineMessageHandler(stateMachine);

		messageListener.addEventHandler(new MessageListener.EventHandler() {
			@Override
			public void onMessageReceived(Packet<Message> message) {
				messageHandler.handleMessage(message);
			}
		});

		executorService.submit(messageSender);
		executorService.submit(messageHandler);
		executorService.submit(messageListener);

		Channel udpChannel;
		try {
			udpChannel = new MessageChannel(new Base64Channel(new UdpChannel(udpSocket)));
		} catch (ChannelException e) {
			e.printStackTrace();
			return;
		}

		udpMessageListener = new ChannelMessageListener(udpChannel);
		udpMessageSender = new ChannelMessageSender(udpChannel);

		executorService.submit(udpMessageSender);
		executorService.submit(udpMessageListener);

		new Thread(shell).start();
	}

	private <RequestType extends Request, ResponseType extends Response> ResponseType syncRequest(RequestType request) throws Exception {
		final AsyncRequest<RequestType, ResponseType> asyncRequest = new AsyncRequest<>(request, messageListener, messageSender);
		final Future<ResponseType> future = executor.submit(asyncRequest);
		return future.get(5, TimeUnit.SECONDS);
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);

		try {
			LoginResponse response = syncRequest(request);
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
			LogoutResponse response = syncRequest(request);
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
			SendMessageResponse response = syncRequest(request);
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
			final AsyncRequest<ListRequest, ListResponse> asyncRequest = new AsyncRequest<>(request, udpMessageListener, udpMessageSender);
			final Future<ListResponse> future = executor.submit(asyncRequest);
			ListResponse response =  future.get(3, TimeUnit.SECONDS);

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
				LookupResponse response = syncRequest(lookupRequest);
				privateAddress = response.getPrivateAddress();
			} catch (Exception e) {
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

		final MessageListener listener = new ChannelMessageListener(channel);
		final MessageSender sender = new ChannelMessageSender(channel);

		executorService.submit(sender);
		executorService.submit(listener);

		String result;
		try {
			final AsyncRequest<SendPrivateMessageRequest, SendPrivateMessageResponse> asyncRequest = new AsyncRequest<>(request, listener, sender);
			final Future<SendPrivateMessageResponse> future = executor.submit(asyncRequest);
			SendPrivateMessageResponse response = future.get(3, TimeUnit.SECONDS);
			result = username + " replied with !ack.";
		} catch (Exception e) {
			result = e.getMessage();
		} finally {
			socket.close();
		}

		return result;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		LookupRequest request = new LookupRequest();
		request.setUsername(username);

		try {
			LookupResponse response = syncRequest(request);
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
			RegisterResponse response = syncRequest(request);
		} catch (Exception e) {
			return e.getMessage();
		}

		final ServerSocket serverSocket = new ServerSocket(address.getPort());
		PrivateMessageSocketListener listener = new PrivateMessageSocketListener(serverSocket, shell, executorService);
		executorService.submit(listener);

		return "Successfully registered address for ME."; // FIXME replace ME with username and start listener
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		return state.getLastPublicMessage();
	}

	@Override
	@Command
	public String exit() throws IOException {
		logout();

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

	private class ClientMainState extends State {
		private String lastPublicMessage = "No message received !";

		@Override
		public StateResult handleMessageEvent(MessageEvent event) throws StateException {
			lastPublicMessage = event.getUsername() + ": " + event.getMessage();

			try {
				shell.writeLine(lastPublicMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return new StateResult(this);
		}

		@Override
		public StateResult handleExitEvent(ExitEvent event) throws StateException {
			try {
				shell.writeLine("Received exit from server, shutdown ...");
				exit();
			} catch (IOException e) {
				System.err.println("calling exit() failed");
			}

			return new StateResult(this);
		}

		public String getLastPublicMessage() {
			return lastPublicMessage;
		}

		@Override
		public String toString() {
			return "client state online";
		}
	}
}
