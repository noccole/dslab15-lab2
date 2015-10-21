package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import channels.ChannelException;
import channels.CommandChannel;
import channels.Channel;
import channels.TcpChannel;
import commands.*;
import executors.CommandBus;
import executors.StateMachineRequestHandler;
import executors.RemoteCommandHandler;
import executors.ChannelRequestListener;
import states.State;
import states.StateException;
import states.StateMachine;
import util.Config;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

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
	public Client(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		// TODO

		Socket socket;
		try {
			socket = new Socket("localhost", 12345);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// add a local command executor
		final StateMachine stateMachine = new StateMachine(new StateOffline());
		clientBus.addCommandExecutor(new StateMachineRequestHandler(stateMachine));

		// add a remote command executor
		Channel commandChannel;
		try {
			commandChannel = new CommandChannel(new TcpChannel(socket));
		} catch (ChannelException e) {
			e.printStackTrace();
			return;
		}
		clientBus.addCommandExecutor(new RemoteCommandHandler(commandChannel));
		clientBus.addCommandListener(new ChannelRequestListener(commandChannel));
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			SendMessageRequest command = new SendMessageRequest();
			command.setMessage(scanner.nextLine());

			System.out.println("--- Send ---");
			clientBus.executeCommand(command);
		}
	}

	@Override
	public String login(String username, String password) throws IOException {
		LoginRequest command = new LoginRequest();
		command.setUsername(username);
		command.setPassword(password);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String logout() throws IOException {
		LogoutRequest command = new LogoutRequest();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String send(String message) throws IOException {
		SendMessageRequest command = new SendMessageRequest();
		command.setMessage(message);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String list() throws IOException {
		ListRequest command = new ListRequest();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String msg(String username, String message) throws IOException {
		SendPrivateMessageRequest command = new SendPrivateMessageRequest();
		command.setReceiver(username);
		command.setMessage(message);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String lookup(String username) throws IOException {
		LookupRequest command = new LookupRequest();
		command.setUsername(username);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String register(String privateAddress) throws IOException {
		RegisterRequest command = new RegisterRequest();
		command.setAddress(privateAddress);
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String lastMsg() throws IOException {
		LastMessageCommand command = new LastMessageCommand();
		clientBus.executeCommand(command);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		// TODO: start the client
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private class StateOffline extends State {
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
	}
}
