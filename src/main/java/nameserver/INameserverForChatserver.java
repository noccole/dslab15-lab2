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

	/**
	 * registers a private address for the specified user in an recursive manner
	 * @param username username of the user
	 * @param address the private address
	 * @throws RemoteException if an error while accessing the remote object occurred
	 * @throws AlreadyRegisteredException if the same address was already registered for this user
	 * @throws InvalidDomainException if a nameserver could not be found
     */
	void registerUser(String username, String address)
			throws RemoteException, AlreadyRegisteredException,
			InvalidDomainException;

	/**
	 * removes the registered privateaddress
	 * @param username username of the user
	 * @throws RemoteException if an error while accessing the remote object occurred
	 * @throws InvalidDomainException if a nameserver could not be found
	 */
	void deregisterUser(String username) throws RemoteException, InvalidDomainException;

	/**
	 * @param zone zone
	 * @return  returns the nameserver for the specified zone
	 * @throws RemoteException
     */
	INameserverForChatserver getNameserver(String zone)
			throws RemoteException;

	/**
	 * @param username user
	 * @return returns the private address of the specified user
	 * (nameserver structure is traversed recursively)
	 * @throws RemoteException
     */
	String lookup(String username) throws RemoteException;

}
