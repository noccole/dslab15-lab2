package service;

import entities.Domain;
import entities.PrivateAddress;
import entities.User;
import nameserver.INameserver;
import nameserver.INameserverForChatserver;
import nameserver.exceptions.AlreadyRegisteredException;
import nameserver.exceptions.InvalidDomainException;
import repositories.RepositoryException;
import repositories.UserRepository;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class UserService {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public interface EventHandler {
        void onUserPresenceChanged(User user);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    private final ConcurrentHashMap<String, User> usersCache = new ConcurrentHashMap<>();

    private INameserver nameserver;

    public UserService(UserRepository userRepository) {

        try {
            final Collection<User> users = userRepository.findAll();
            for (User user : users) {
                usersCache.put(user.getUsername(), user);
            }
        } catch (RepositoryException e) {
            LOGGER.warning("could not load users: " + e);
        }
    }

    public void setRootNameserver(INameserver nameserver) {
        this.nameserver = nameserver;
    }

    /**
     * Login the User \a user if the password matches and the user is offline atm.
     *
     * User presence will be changed to Presence.Available
     *
     * @param user
     * @param password
     * @return True if the user was successfully logged in
     */
    public boolean login(final User user, String password) {
        synchronized (usersCache) {
            if (user.getPresence() == User.Presence.Offline && user.getPassword().equals(password)) {
                user.setPresence(User.Presence.Available);
                emitUserPresenceChanged(user);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Logout the User \a user if the user isn't offline already.
     *
     * User presence will be changed to Presence.Offline and all registered private addresses will be removed.
     *
     * @param user
     */
    public void logout(final User user) {
        synchronized (usersCache) {
            if (user.getPresence() != User.Presence.Offline) {
                user.setPresence(User.Presence.Offline);
                try {
                    nameserver.deregisterUser(user.getUsername());
                } catch (RemoteException | InvalidDomainException e) {
                    LOGGER.warning("deregister private address of user '" + user + "' failed");
                }
                emitUserPresenceChanged(user);
            }
        }
    }

    /**
     * Find all existing users
     *
     * @return A collection of all existing users
     */
    public Collection<User> findAll() {
        return usersCache.values();
    }

    /**
     * Find the user with the given username
     *
     * @param username
     * @return User instance if found, null if not found.
     */
    public User find(String username) {
        return usersCache.get(username);
    }

    /**
     * registers the private address of the specified user;
     * overrides previously registered addresses
     * @param user user
     * @param privateAddress private address to register
     * @throws ServiceException if the new address was already registered for this user
     *                          if an connection error to the name server occurred
     *                          if the username is invalid
     */
    public void registerPrivateAddress(User user, PrivateAddress privateAddress) throws ServiceException {
        LOGGER.info("register private address of user '" + user.getUsername() + "'");
        try {
            nameserver.registerUser(user.getUsername(), privateAddress.toString());
        } catch (RemoteException e) {
            throw new ServiceException("error connecting to name server", e);
        } catch (AlreadyRegisteredException | InvalidDomainException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * @param user user
     * @return returns the previously registered private address for the specified user
     * @throws ServiceException if an connection error to the name server occurred
     */
    public PrivateAddress lookupPrivateAddress(User user) throws ServiceException {
        LOGGER.info("lookup private address of user '" + user.getUsername() + "'");
        Domain domain = new Domain(user.getUsername());
        INameserverForChatserver currentServer = nameserver;

        try {
            while(currentServer != null && domain.hasSubdomain()) {
                currentServer = currentServer.getNameserver(domain.root());
                domain = domain.subdomain();
            }
            // name server not reachable
            if (currentServer == null) {
                throw new ServiceException("nameserver not reachable");
            }
            String privateAddress = currentServer.lookup(domain.toString());
            // private address not registered
            if (privateAddress == null) {
                throw new ServiceException("user '" + user.getUsername() + "' not reachable");
            }
            return new PrivateAddress(privateAddress);
        } catch (RemoteException e) {
            throw new ServiceException("error connecting to name server", e);
        }
    }

    /**
     * @return A list of user names and their presence
     */
    public Map<String, User.Presence> getUserList() {
        final List<User> users = new LinkedList(findAll());

        final Map<String, User.Presence> userStates = new TreeMap<>();
        synchronized (usersCache) {
            for (User user : users) {
                userStates.put(user.getUsername(), user.getPresence());
            }
        }

        return userStates;
    }

    private void emitUserPresenceChanged(User user) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onUserPresenceChanged(user);
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }
    }

    public void removeEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }
    }
}
