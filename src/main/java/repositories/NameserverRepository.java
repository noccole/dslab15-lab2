package repositories;

import nameserver.INameserver;
import nameserver.INameserverForChatserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * repository for the nameservers
 */
public class NameserverRepository implements INameserverRepository {
    private final ConcurrentHashMap<String, INameserver> nameservers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, INameserverForChatserver> nameserversForChatserver = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean add(String zone, INameserver nameserver, INameserverForChatserver nameserverForChatserver) {
        zone = zone.toLowerCase();
        if (nameservers.containsKey(zone)) {
            return false;
        }
        nameservers.put(zone, nameserver);
        nameserversForChatserver.put(zone, nameserverForChatserver);
        return true;
    }

    @Override
    public boolean contains(String zone) {
        return nameservers.containsKey(zone.toLowerCase());
    }

    @Override
    public INameserver getNameserver(String zone) {
        return nameservers.get(zone.toLowerCase());
    }

    @Override
    public INameserverForChatserver getNameserverForChatserver(String zone) {
        return nameserversForChatserver.get(zone.toLowerCase());
    }

    @Override
    public Collection<String> registeredZones() {
        return nameservers.keySet();
    }
}
