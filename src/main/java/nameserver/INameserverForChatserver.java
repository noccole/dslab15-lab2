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
	 * @throws RemoteException
	 * @throws AlreadyRegisteredException
	 * @throws InvalidDomainException
     */
	void registerUser(String username, String address)
			throws RemoteException, AlreadyRegisteredException,
			InvalidDomainException;

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
