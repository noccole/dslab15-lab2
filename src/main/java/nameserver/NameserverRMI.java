package nameserver;

import entities.Domain;
import entities.PrivateAddress;
import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import repositories.INameserverRepository;
import repositories.IPrivateAddressRepository;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Encapsulates the nameserver's remotely executable methods
 */
public class NameserverRMI implements INameserver {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    private final IPrivateAddressRepository privateAddressRepository;
    private final INameserverRepository nameserverRepository;

    public NameserverRMI(INameserverRepository nameserverRepository, IPrivateAddressRepository privateAddressRepository) {
        this.nameserverRepository = nameserverRepository;
        this.privateAddressRepository = privateAddressRepository;
    }

    @Override
    public void registerNameserver(String domainString, INameserver nameserver, INameserverForChatserver nameserverForChatserver) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        LOGGER.info("register nameserver for domain '" + domainString + "'");

        final Domain domain = new Domain(domainString);
        if (!domain.isValid()) {
            throw new InvalidDomainException("domain '" + domain + "' not valid");
        }

        if (domain.hasSubdomain()) {
            // get responsible nameserver
            getNameserverFromRepository(domain.root()).registerNameserver(domain.subdomain().toString(), nameserver, nameserverForChatserver);
        } else if (nameserverRepository.contains(domain.toString())) {
            // name server already registered
            throw new AlreadyRegisteredException("domain '" + domain + "' already registered");
        } else {
            // register name server as child of this server
            nameserverRepository.add(domain.toString(), nameserver, nameserverForChatserver);
        }
    }

    @Override
    public void registerUser(String username, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        LOGGER.info("register private address of user '" + username + "'");

        final Domain domain = new Domain(username);
        if (domain.hasSubdomain()) {
            // get responsible nameserver
            getNameserverFromRepository(domain.root()).registerUser(domain.subdomain().toString(), address);
        } else if (privateAddressRepository.contains(username)) {
            // user has already registered a private address
            throw new AlreadyRegisteredException("private address '" + address + "' for user '" + username + "' already registered");
        } else {
            // register private address
            privateAddressRepository.add(username, new PrivateAddress(address));
        }
    }

    @Override
    public void deregisterUser(String username) throws RemoteException, InvalidDomainException {
        LOGGER.info("deregister private address of user '" + username + "'");

        final Domain domain = new Domain(username);
        if (domain.hasSubdomain()) {
            getNameserverFromRepository(domain.root()).deregisterUser(domain.subdomain().toString());
        } else {
            privateAddressRepository.remove(username);
        }
    }

    @Override
    public INameserverForChatserver getNameserver(String zone) throws RemoteException {
        LOGGER.info("return nameserver for zone '" + zone + "'");

        // return nameserver if zone is managed by this nameserver, otherwise return null
        return nameserverRepository.getNameserverForChatserver(zone);
    }

    @Override
    public String lookup(String username) throws RemoteException {
        LOGGER.info("return private address for user '" + username + "'");

        if (!privateAddressRepository.contains(username)) {
            return null;
        }
        return privateAddressRepository.getPrivateAddress(username).toString();
    }

    /**
     * @param zone zone
     * @return returns the nameserver for the specified zone from the repository
     * @throws InvalidDomainException if the repository contains no nameserver for the specified zone
     */
    private INameserver getNameserverFromRepository(String zone) throws InvalidDomainException {
        if (nameserverRepository.contains(zone)) {
            return nameserverRepository.getNameserver(zone);
        } else {
            throw new InvalidDomainException("nameserver for zone '" + zone + "' not found");
        }
    }
}
