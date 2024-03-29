package client;

import channels.*;
import cli.Command;
import cli.Shell;
import entities.PrivateAddress;
import entities.User;
import messages.*;
import shared.*;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client implements IClientCli, Runnable {
	private static final Logger LOGGER = Logger.getAnonymousLogger();

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Shell shell;
	private final Config config;

	private String username;
	private String lastPublicMessage = "No message received !";

	private ClientHandler tcpRequester;
	private ClientHandler udpRequester;

	private final HandlerManager handlerManager;
	private final PrivateMessageChannelFactory privateMessageChannelFactory;

	private Collection<SocketConnectionListener> socketListeners = new LinkedList<>();

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

		LogManager.getLogManager().reset(); // disable logging

		handlerManager = new HandlerManager();

		final MacFactory macFactory = new MacFactory(config.getString("hmac.key"));
		privateMessageChannelFactory = new PrivateMessageChannelFactory(macFactory);

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	private boolean startTcpHandler() {
		final Socket socket;
		try {
			socket = new Socket(config.getString("chatserver.host"), config.getInt("chatserver.tcp.port"));
		} catch (IOException e) {
			LOGGER.warning("could not open tcp socket");
			return false;
		}

		final Channel channel;
		try {
			channel = MessageChannelFactory.create(new Base64Channel(new TcpChannel(socket)));
		} catch (ChannelException e) {
			LOGGER.warning("could not create tcp channel");
			return false;
		}

		tcpRequester = new ClientHandler(channel, executorService, handlerManager);
		tcpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				lastPublicMessage = message;

				try {
					shell.writeLine(message);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
				}
			}

			@Override
			public void onTamperedMessageReceived(String message) {
				try {
					shell.writeLine("[TAMPERED-MSG] " + message);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
				}
			}

			@Override
			public void onPresenceChanged(String presenceMessage) {
				try {
					shell.writeLine(presenceMessage);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
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
			LOGGER.warning("could not open udp socket");
			return false;
		}

		final Channel channel;
		try {
			channel = MessageChannelFactory.create(new Base64Channel(new UdpChannel(socket)));
		} catch (ChannelException e) {
			LOGGER.warning("could not create udp channel");
			return false;
		}

		udpRequester = new ClientHandler(channel, executorService, handlerManager);
		udpRequester.addEventHandler(new ClientHandlerBase.EventHandler() {
			@Override
			public void onMessageReceived(String message) {
				try {
					shell.writeLine(message);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
				}
			}

			@Override
			public void onTamperedMessageReceived(String message) {
				try {
					shell.writeLine("[TAMPERED-MSG] " + message);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
				}
			}

			@Override
			public void onPresenceChanged(String presenceMessage) {
				try {
					shell.writeLine(presenceMessage);
				} catch (IOException e) {
					LOGGER.warning("could not write message");
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
		if (!startUdpHandler()) {
			exit();
		}
		if (!startTcpHandler()) {
			exit();
		}

		new Thread(shell).start();
	}

	@Override
	@Command
	public String login(String username, String password) {
		final LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);

		try {
			final LoginResponse response = tcpRequester.syncRequest(request, LoginResponse.class);
			switch (response.getResponse()) {
				case Success:
					this.username = username;
					return "Successfully logged in.";
				case WrongPassword:	// fall through
				case UnknownUser:
					return "Wrong username or password.";
				case UserAlreadyLoggedIn:
					return "User is already logged in.";
				default:
					return "Unknown error.";
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
			final LogoutResponse response = tcpRequester.syncRequest(request, LogoutResponse.class);
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
			final SendMessageResponse response = tcpRequester.syncRequest(request, SendMessageResponse.class);
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
			final ListResponse response =  udpRequester.syncRequest(request, ListResponse.class);

			String result = "Online users:\n";
			for (Map.Entry<String, User.Presence> entry : response.getUserList().entrySet()) {
				final String username = entry.getKey();
				final User.Presence presence = entry.getValue();

				if (presence == User.Presence.Available) {
					result += "* " + username + "\n";
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
		final PrivateAddress privateAddress;
		{
			final LookupRequest lookupRequest = new LookupRequest();
			lookupRequest.setUsername(username);

			try {
				final LookupResponse response = tcpRequester.syncRequest(lookupRequest, LookupResponse.class);
				privateAddress = response.getPrivateAddress();
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		final Socket socket;
		try {
			socket = new Socket(privateAddress.getHostname(), privateAddress.getPort());
		} catch (IOException e) {
			LOGGER.warning("could not open private message socket: " + e);
			return "Wrong username or user not reachable.";
		}

		final Channel channel;
		try {
			channel = privateMessageChannelFactory.createChannel(socket);
		} catch (ChannelException e) {
			LOGGER.warning("could not create private message channel: " + e);
			try {
				socket.close();
			} catch (IOException e1) {
				LOGGER.warning("could not close private message socket: " + e1);
			}
			return "Wrong username or user not reachable.";
		}

		final ClientHandler requester = new ClientHandler(channel, executorService, handlerManager);
		try {
			final SendPrivateMessageResponse response = requester.syncRequest(request, SendPrivateMessageResponse.class);
			requester.stop();
			return username + " replied with !ack."; // success
		} catch (Exception e) {
			requester.stop();
			return e.getMessage(); // error, abort
		}
	}

	@Override
	@Command
	public String lookup(String username) {
		final LookupRequest request = new LookupRequest();
		request.setUsername(username);

		try {
			final LookupResponse response = tcpRequester.syncRequest(request, LookupResponse.class);
			return String.valueOf(response.getPrivateAddress()) + "\n";
		} catch (Exception e) {
			return e.getMessage();
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
			final RegisterResponse response = tcpRequester.syncRequest(request, RegisterResponse.class);
		} catch (Exception e) {
			try {
				serverSocket.close();
			} catch (IOException e1) {
				LOGGER.warning("Could not close private message server socket");
			}
			return e.getMessage();
		}

		final SocketConnectionListener listener = new SocketConnectionListener(serverSocket, new HandlerFactory() {
			@Override
			public HandlerBase createHandler(Channel channel) {
				final PrivateMessageHandler handler = new PrivateMessageHandler(channel, executorService, handlerManager);
				handler.addEventHandler(new ClientHandlerBase.EventHandler() {
					@Override
					public void onMessageReceived(String message) {
						try {
							shell.writeLine("[PRV] " + message);
						} catch (IOException e) {
							LOGGER.warning("could not write message");
						}
					}

					@Override
					public void onTamperedMessageReceived(String message) {
						try {
							shell.writeLine("[TAMPERED-PRV-MSG] " + message);
						} catch (IOException e) {
							LOGGER.warning("could not write message");
						}
					}

					@Override
					public void onPresenceChanged(String presenceMessage) {

					}

					@Override
					public void onExit() {

					}
				});

				return handler;
			}
		}, privateMessageChannelFactory);
		socketListeners.add(listener);
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

		executorService.shutdown();

		for (SocketConnectionListener socketListener : socketListeners) {
			socketListener.cancel();
		}

		handlerManager.stopAllHandlers();

		try {
			if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			LOGGER.warning("Could not shutdown executor service, force it ...");
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
