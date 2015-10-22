package nameserver;

import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Please note that this interface is not needed for Lab 1, but will
 * later be used in Lab 2. Hence, you do not have to implement it for the
 * first submission.
 */
public interface INameserver extends INameserverForChatserver, Remote {

	void registerNameserver(String domain, INameserver nameserver,
							INameserverForChatserver nameserverForChatserver)
			throws RemoteException, AlreadyRegisteredException,
			InvalidDomainException;

}
