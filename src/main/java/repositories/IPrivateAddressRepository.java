package repositories;

import entities.PrivateAddress;

import java.util.Collection;
import java.util.HashMap;

/**
 * repository for the private address of users
 */
public interface IPrivateAddressRepository {
    /**
     * adds the private address of the specified user to the repository, replacing
     * previous values
     * @param user user
     * @param privateAddress private address
     */
    void add(String user, PrivateAddress privateAddress);

    /**
     * removes the registered private address of the specified user
     * @param user user
     */
    void remove(String user);

    /**
     * @param user user
     * @return true if an private address for the specified user exists
     */
    boolean contains(String user);

    /**
     * @param user user
     * @return returns the private address of the specified user or null if no address found
     */
    PrivateAddress getPrivateAddress(String user);

    /**
     * @return returns all stored addresses
     */
    HashMap<String, PrivateAddress> getAll();
}
