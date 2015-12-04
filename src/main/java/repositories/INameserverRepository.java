package repositories;

import nameserver.INameserver;
import nameserver.INameserverForChatserver;

import java.util.Collection;

/**
 * repository for the nameservers
 */
public interface INameserverRepository {
    /**
     * adds a nameserver to the repository, if no nameserver for this zone exists
     * @param zone zone of the nameserver
     * @param nameserver
     * @param nameserverForChatserver
     * @return true if adding was successful
     */
    boolean add(String zone, INameserver nameserver, INameserverForChatserver nameserverForChatserver);

    /**
     * @param zone zone
     * @return true if a nameserver for the specified zone exists
     */
    boolean contains(String zone);

    /**
     * @param zone zone
     * @return returns the nameserver for the specified zone
     */
    INameserver getNameserver(String zone);

    /**
     * @param zone zone
     * @return returns the nameserver for the specified zone
     */
    INameserverForChatserver getNameserverForChatserver(String zone);

    /**
     * @return returns a list of all registered zones
     */
    Collection<String> registeredZones();
}
