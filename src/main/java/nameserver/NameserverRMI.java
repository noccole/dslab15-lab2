package nameserver;

import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import repositories.UserRepository;

import java.rmi.RemoteException;

/**
 * Encapsulates the nameserver's remotely executable methods
 */
public class NameserverRMI implements INameserver {
    private UserRepository userRepository;

    NameserverRMI(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void registerNameserver(String domain, INameserver nameserver, INameserverForChatserver nameserverForChatserver) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {

    }

    @Override
    public void registerUser(String username, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {

    }

    @Override
    public INameserverForChatserver getNameserver(String zone) throws RemoteException {
        return null;
    }

    @Override
    public String lookup(String username) throws RemoteException {
        return null;
    }
}
