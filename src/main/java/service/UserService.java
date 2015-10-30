package service;

import entities.User;
import repositories.RepositoryException;
import repositories.UserRepository;

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
        synchronized (user) {
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
        synchronized (user) {
            if (user.getPresence() != User.Presence.Offline) {
                user.clearPrivateAddresses();
                user.setPresence(User.Presence.Offline);
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
     * @return A list of user names and their presence
     */
    public Map<String, User.Presence> getUserList() {
        final List<User> users = new LinkedList(findAll());

        final Map<String, User.Presence> userStates = new TreeMap<>();
        for (User user : users) {
            userStates.put(user.getUsername(), user.getPresence());
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
