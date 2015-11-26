package nameserver;

import java.util.Collection;

/**
 * repository for the nameservers
 */
public interface INameserverRepository {
    boolean add(String zone, INameserver nameserver, INameserverForChatserver nameserverForChatserver);

    boolean contains(String zone);

    INameserver getNameserver(String zone);

    INameserverForChatserver getNameserverForChatserver(String zone);

    Collection<String> registeredZones();
}
