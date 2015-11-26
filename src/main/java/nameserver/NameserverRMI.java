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
    private IPrivateAddressRepository privateAddressRepository;
    private INameserverRepository nameserverRepository;

    public NameserverRMI(INameserverRepository nameserverRepository, IPrivateAddressRepository privateAddressRepository) {
        this.nameserverRepository = nameserverRepository;
        this.privateAddressRepository = privateAddressRepository;
    }

    @Override
    public void registerNameserver(String domain_string, INameserver nameserver, INameserverForChatserver nameserverForChatserver) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        Domain domain = new Domain(domain_string);
        if (!domain.isValid()) {
            throw new InvalidDomainException("domain '" + domain + "' not valid");
        }

        if (domain.hasSubdomain()) {
            if (nameserverRepository.contains(domain.root())) {
                // send register request to child server
                nameserverRepository.getNameserver(domain.root()).registerNameserver(domain.subdomain().toString(), nameserver, nameserverForChatserver);
            } else {
                // no suitable child server registered -> abort
                throw new InvalidDomainException("no nameserver for zone '" + domain.root() + "' registered ");
            }
        } else {
            if (nameserverRepository.contains(domain.toString())) {
                // name server already registered
                throw new AlreadyRegisteredException("domain '" + domain + "' already registered");
            }

            // register name server as child of this server
            nameserverRepository.add(domain.toString(), nameserver, nameserverForChatserver);
            LOGGER.info("registered domain '" + domain.toString() + "'");
        }
    }

    @Override
    public void registerUser(String username, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        Domain domain = new Domain(username);
        if (domain.hasSubdomain()) {
            if (nameserverRepository.contains(domain.root())) {
                nameserverRepository.getNameserverForChatserver(domain.root()).registerUser(domain.subdomain().toString(), address);
            } else {
                throw new InvalidDomainException("nameserver for zone '" + domain.root() + "' not found");
            }
        } else {
            privateAddressRepository.add(username, new PrivateAddress(address));
        }
    }

    @Override
    public INameserverForChatserver getNameserver(String zone) throws RemoteException {
        return nameserverRepository.getNameserverForChatserver(zone);
    }

    @Override
    public String lookup(String username) throws RemoteException {
        return privateAddressRepository.getPrivateAddress(username).toString();
    }
}
