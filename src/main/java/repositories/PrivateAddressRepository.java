package repositories;

import entities.PrivateAddress;

import java.util.concurrent.ConcurrentHashMap;

/**
 * repository for the private address of users
 */
public class PrivateAddressRepository implements IPrivateAddressRepository {
    private ConcurrentHashMap<String, PrivateAddress> privateAddresses = new ConcurrentHashMap<>();

    @Override
    public void add(String user, PrivateAddress privateAddress) {
        privateAddresses.put(user, privateAddress);
    }

    @Override
    public boolean contains(String user) {
        return privateAddresses.containsKey(user);
    }

    @Override
    public PrivateAddress getPrivateAddress(String user) {
        return privateAddresses.get(user);
    }
}
