package client;

import cli.Command;
import cli.Shell;
import commands.*;
import executors.CommandBus;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Client implements IClientCli, Runnable {
	private final Shell shell;
	private final Config config;

	private final CommandBus clientBus = new CommandBus();

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
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		LoginRequest command = new LoginRequest();
		command.setUsername(username);
		command.setPassword(password);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String logout() throws IOException {
		LogoutRequest command = new LogoutRequest();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		SendMessageRequest command = new SendMessageRequest();
		command.setMessage(message);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String list() throws IOException {
		ListRequest command = new ListRequest();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		SendPrivateMessageRequest command = new SendPrivateMessageRequest();
		command.setReceiver(username);
		command.setMessage(message);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		LookupRequest command = new LookupRequest();
		command.setUsername(username);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		RegisterRequest command = new RegisterRequest();
		command.setAddress(privateAddress);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		LastMessageCommand command = new LastMessageCommand();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String exit() throws IOException {
		ExitCommand command = new ExitCommand();
		clientBus.executeCommand(command);

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
		public State handleLoginRequest(LoginRequest command) throws StateException {
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
	}

	private class StateOnline extends State {
		private String lastPublicMessage = "No message received !";

		@Override
		public State handleLogoutRequest(LogoutRequest command) throws StateException {
			return new StateLoggingOut();
		}

		@Override
		public State handleSendMessageRequest(SendMessageRequest command) throws StateException {
			System.out.println(command.getMessage());
			lastPublicMessage = command.getMessage();
			return this;
		}

		@Override
		public State handleSendPrivateMessageRequest(SendPrivateMessageRequest command) throws StateException {
			System.out.println("PRIVATE " + command.getMessage());
			return this;
		}

		@Override
		public State applyLastMessageCommand(LastMessageCommand command) throws StateException {
			System.out.println(lastPublicMessage);
			return this;
		}

		@Override
		public State applyExitCommand(ExitCommand command) throws StateException {
			return this;
		}

		@Override
		public String toString() {
			return "client state online";
		}
	}    */
}
