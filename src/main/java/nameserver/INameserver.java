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

	/**
	 * registers a new nameserver with the specified domain recursively.
	 * all servers between the root server and the server to add have to exist.
	 *
	 * @param domain
	 * @param nameserver
	 * @param nameserverForChatserver
	 * @throws RemoteException
	 * @throws AlreadyRegisteredException
	 * @throws InvalidDomainException
     */
	void registerNameserver(String domain, INameserver nameserver,
							INameserverForChatserver nameserverForChatserver)
			throws RemoteException, AlreadyRegisteredException,
			InvalidDomainException;

}
