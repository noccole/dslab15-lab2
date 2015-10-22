package client;

import channels.*;
import cli.Command;
import cli.Shell;
import commands.*;
import executors.ChannelMessageSender;
import executors.MessageSender;
import states.State;
import states.StateException;
import states.StateResult;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Client implements IClientCli, Runnable {
	private final Shell shell;
	private final Config config;

	private MessageSender messageSender;

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

		messageSender = new ChannelMessageSender(channel);
	}

	private void sendRequest(Request request) {
		Packet<Message> packet = new NetworkPacket<>();
		packet.pack(request);
		messageSender.sendMessage(packet);
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);
		sendRequest(request);


		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		LogoutRequest request = new LogoutRequest();
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		SendMessageRequest request = new SendMessageRequest();
		request.setMessage(message);
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		ListRequest request = new ListRequest();
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		SendPrivateMessageRequest request = new SendPrivateMessageRequest();
		request.setReceiver(username);
		request.setMessage(message);
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		LookupRequest request = new LookupRequest();
		request.setUsername(username);
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		RegisterRequest request = new RegisterRequest();
		request.setAddress(privateAddress);
		sendRequest(request);

		// TODO Auto-generated method stub
		return null;
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

	/*private class StateOffline extends State {
		@Override
		public State handleLoginRequest(LoginRequest request) throws StateException {
			return new StateLoggingIn();
		}

		@Override
		public String toString() {
			return "client state offline";
		}
	}

	private class StateLoggingIn extends State {
		@Override
		public State applyLoginResult(LoginResponse result) throws StateException {
			if (result.isSuccess()) {
				System.out.println("Successfully logged in.");
				return new StateOnline();
			} else {
				System.out.println("Wrong username or password.");
				return new StateOffline();
			}
		}

		@Override
		public String toString() {
			return "client state logging in";
		}
	}

	private class StateLoggingOut extends State {
		@Override
		public State applyLogoutResult(LogoutResponse result) throws StateException {
			System.out.println("Successfully logged out.");
			return new StateOffline();
		}

		@Override
		public String toString() {
			return "client state logging out";
		}
	}  */

	private class StateOnline extends State {
		private String lastPublicMessage = "No message received !";

		@Override
		public StateResult handleMessageEvent(MessageEvent event) throws StateException {
			try {
				shell.writeLine(event.getUsername() + ": " + event.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return new StateResult(this);
		}

		/*@Override
		public State handleLogoutRequest(LogoutRequest request) throws StateException {
			return new StateLoggingOut();
		}

		@Override
		public State handleSendMessageRequest(SendMessageRequest request) throws StateException {
			System.out.println(request.getMessage());
			lastPublicMessage = request.getMessage();
			return this;
		}

		@Override
		public State handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
			System.out.println("PRIVATE " + request.getMessage());
			return this;
		}

		@Override
		public State applyLastMessageCommand(LastMessageCommand request) throws StateException {
			System.out.println(lastPublicMessage);
			return this;
		}

		@Override
		public State applyExitCommand(ExitCommand request) throws StateException {
			return this;
		} */

		@Override
		public String toString() {
			return "client state online";
		}
	}
}
