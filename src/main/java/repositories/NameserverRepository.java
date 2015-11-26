package repositories;

import nameserver.INameserver;
import nameserver.INameserverForChatserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * repository for the nameservers
 */
public class NameserverRepository implements INameserverRepository {
    private ConcurrentHashMap<String, NameserverRMIInterfaces> servers = new ConcurrentHashMap<>();

    @Override
    public boolean add(String zone, INameserver nameserver, INameserverForChatserver nameserverForChatserver) {
        if (contains(zone)) {
            return false;
        }
        servers.put(zone, new NameserverRMIInterfaces(nameserver, nameserverForChatserver));
        return true;
    }

    @Override
    public boolean contains(String zone) {
        return servers.containsKey(zone);
    }

    @Override
    public INameserver getNameserver(String zone) {
        if (!contains(zone)) {
            return null;
        }
        return servers.get(zone).getNameserver();
    }

    @Override
    public INameserverForChatserver getNameserverForChatserver(String zone) {
        if (!contains(zone)) {
            return null;
        }
        return servers.get(zone).getNameserverForChatserver();
    }

    @Override
    public Collection<String> registeredZones() {
        return servers.keySet();
    }

    public class NameserverRMIInterfaces {
        private INameserver nameserver;
        private INameserverForChatserver nameserverForChatserver;

        public NameserverRMIInterfaces(INameserver nameserver, INameserverForChatserver nameserverForChatserver) {
            this.nameserver = nameserver;
            this.nameserverForChatserver = nameserverForChatserver;
        }

        public INameserver getNameserver() {
            return nameserver;
        }

        public INameserverForChatserver getNameserverForChatserver() {
            return nameserverForChatserver;
        }
    }
}
