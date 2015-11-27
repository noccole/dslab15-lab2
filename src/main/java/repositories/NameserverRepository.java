package repositories;

import nameserver.INameserver;
import nameserver.INameserverForChatserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * repository for the nameservers
 */
public class NameserverRepository implements INameserverRepository {
    private ConcurrentHashMap<String, INameserver> nameservers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, INameserverForChatserver> nameserversForChatserver = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean add(String zone, INameserver nameserver, INameserverForChatserver nameserverForChatserver) {
        if (nameservers.containsKey(zone)) {
            return false;
        }
        nameservers.put(zone, nameserver);
        nameserversForChatserver.put(zone, nameserverForChatserver);
        return true;
    }

    @Override
    public boolean contains(String zone) {
        return nameservers.containsKey(zone);
    }

    @Override
    public INameserver getNameserver(String zone) {
        return nameservers.get(zone);
    }

    @Override
    public INameserverForChatserver getNameserverForChatserver(String zone) {
        return nameserversForChatserver.get(zone);
    }

    @Override
    public Collection<String> registeredZones() {
        return nameservers.keySet();
    }
}
