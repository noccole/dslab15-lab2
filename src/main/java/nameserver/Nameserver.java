package nameserver;

import cli.Command;
import cli.Shell;
import entities.PrivateAddress;
import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import repositories.INameserverRepository;
import repositories.IPrivateAddressRepository;
import repositories.NameserverRepository;
import repositories.PrivateAddressRepository;
import util.Config;

import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Please note that this class is not needed for Lab 1, but will later be used
 * in Lab 2. Hence, you do not have to implement it for the first submission.
 */
public class Nameserver implements INameserverCli, Runnable {
	private static final Logger LOGGER = Logger.getAnonymousLogger();

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Shell shell;
	private NameserverRMI nameserverRMI;
	private INameserverRepository nameserverRepository = new NameserverRepository();
	private IPrivateAddressRepository privateAddressRepository = new PrivateAddressRepository();


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
	public Nameserver(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		LogManager.getLogManager().reset(); // disable logging

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);

		nameserverRMI = new NameserverRMI(nameserverRepository, privateAddressRepository);
	}

	private boolean registerAsRoot() {
		try {
			Registry registry = LocateRegistry.createRegistry(config.getInt("registry.port"));
			INameserver nameserverRMIExported = (INameserver) UnicastRemoteObject.exportObject(nameserverRMI, 0);
			registry.bind(config.getString("root_id"), nameserverRMIExported);
		} catch (RemoteException | AlreadyBoundException e) {
			LOGGER.log(Level.WARNING, "register as root nameserver failed", e);
			return false;
		}

		return true;
	}

	private boolean register() {
		if (!config.listKeys().contains("domain")) { // root name server
			return registerAsRoot();
		}

		// register as non-root nameserver
		try {
			Registry registry = LocateRegistry.getRegistry(config.getString("registry.host"), config.getInt("registry.port"));
			INameserver root = (INameserver) registry.lookup(config.getString("root_id"));
			INameserver nameserverRMIExported = (INameserver) UnicastRemoteObject.exportObject(nameserverRMI, 0);
			root.registerNameserver(config.getString("domain"), nameserverRMIExported, nameserverRMIExported);
		} catch (RemoteException | NotBoundException | InvalidDomainException | AlreadyRegisteredException e) {
			LOGGER.log(Level.WARNING, "register as non-root nameserver for domain '" + config.getString("domain") + "'failed", e);
			return false;
		}

		return true;
	}

	@Override
	public void run() {
		if (!register()) {
			exit();
		}

		new Thread(shell).start();
	}

	@Command
	@Override
	public String nameservers() {
		ArrayList<String> zones = new ArrayList<>(nameserverRepository.registeredZones());
		Collections.sort(zones);

		String result = "";
		for (String zone : zones) {
			result += "* " + zone + "\n";
		}
		return result;
	}

	@Command
	@Override
	public String addresses() {
		HashMap<String, PrivateAddress> privateAddresses = privateAddressRepository.getAll();
		List<String> users = new ArrayList<>(privateAddresses.keySet());
		Collections.sort(users);

		String result = "";
		for (String user : users) {
			result += "* " + user + privateAddresses.get(user) + "\n";
		}
		return result;
	}

	@Command
	@Override
	public String exit() {
		shell.close();

		try {
			if (!UnicastRemoteObject.unexportObject(nameserverRMI, false)) {
				// force unexport
				if (!UnicastRemoteObject.unexportObject(nameserverRMI, true)) {
					LOGGER.info("unexporting remote object failed");
				}
            }
		} catch (NoSuchObjectException e) {
			LOGGER.log(Level.WARNING, "unexporting remote object failed", e);
		}

		return "Shut down completed! Bye ..";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Nameserver}
	 *            component
	 */
	public static void main(String[] args) {
		Nameserver nameserver = new Nameserver(args[0], new Config(args[0]),
				System.in, System.out);
		nameserver.run();
	}

}
