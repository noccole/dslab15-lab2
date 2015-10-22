package client;

import channels.Channel;
import channels.ChannelException;
import channels.MessageChannel;
import channels.TcpChannel;
import cli.Command;
import cli.Shell;
import commands.*;
import entities.User;
import executors.ChannelMessageListener;
import executors.ChannelMessageSender;
import executors.MessageListener;
import executors.MessageSender;
import states.State;
import states.StateException;
import states.StateResult;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Client implements IClientCli, Runnable {
	private final Shell shell;
	private final Config config;

	private MessageListener messageListener;
	private MessageSender messageSender;
	private ExecutorService executor = Executors.newCachedThreadPool();

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

		Channel channel;
		try {
			channel = new MessageChannel(new TcpChannel(socket));
		} catch (ChannelException e) {
			e.printStackTrace();
			return;
		}

		messageListener = new ChannelMessageListener(channel);
		messageSender = new ChannelMessageSender(channel);

		new Thread(shell).start();
	}

	private <RequestType extends Request, ResponseType extends Response> ResponseType syncRequest(RequestType request) throws Exception {
		final AsyncRequest<RequestType, ResponseType> asyncRequest = new AsyncRequest<>(request, messageListener, messageSender);
		final Future<ResponseType> future = executor.submit(asyncRequest);
		return future.get(3, TimeUnit.SECONDS);
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);

		try {
			LoginResponse response = syncRequest(request);
			return response.isSuccess() ? "Successfully logged in." : "Wrong username or password.";
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
			ListResponse response = syncRequest(request);

			final String result = "Online users:\n";
			response.getUserList().forEach(new BiConsumer<String, User.Presence>() {
				@Override
				public void accept(String username, User.Presence presence) {
					if (presence == User.Presence.Available) {
						result.concat(username + "\n"); // FIXME
					}
				}
			});

			return result;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		SendPrivateMessageRequest request = new SendPrivateMessageRequest();
		request.setReceiver(username);
		request.setMessage(message);

		// FIXME lookup and send via private message sender

		try {
			SendPrivateMessageResponse response = syncRequest(request);
			return username + " replied with !ack.";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		LookupRequest request = new LookupRequest();
		request.setUsername(username);

		try {
			LookupResponse response = syncRequest(request);
			return response.getPrivateAddress();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		RegisterRequest request = new RegisterRequest();
		request.setAddress(privateAddress);

		try {
			RegisterResponse response = syncRequest(request);
			return "Successfully registered address for ME."; // FIXME replace ME with username and start listener
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
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

	private class StateOnline extends State {
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
		public String toString() {
			return "client state online";
		}
	}
}
