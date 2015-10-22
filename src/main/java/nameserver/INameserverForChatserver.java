package nameserver;

import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Please note that this interface is not needed for Lab 1, but will later be
 * used in Lab 2. Hence, you do not have to implement it for the first
 * submission.
 */
public interface INameserverForChatserver extends Remote {

	void registerUser(String username, String address)
			throws RemoteException, AlreadyRegisteredException,
			InvalidDomainException;

	INameserverForChatserver getNameserver(String zone)
			throws RemoteException;

	String lookup(String username) throws RemoteException;

}
