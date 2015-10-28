package service;

import entities.User;
import repositories.RepositoryException;
import repositories.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final ConcurrentHashMap<String, User> usersCache = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository) /*throws ServiceException*/ {
        try {
            final Collection<User> users = userRepository.findAll();
            for (User user : users) {
                usersCache.put(user.getUsername(), user);
            }
        } catch (RepositoryException e) {
            System.err.println("could not load users: " + e);
            //throw new ServiceException("could not load users", e);
        }
    }

    public boolean login(final User user, String password) {
        synchronized (user) {
            if (user.getPresence() == User.Presence.Offline && user.getPassword().equals(password)) {
                user.setPresence(User.Presence.Available);
                return true;
            } else {
                return false;
            }
        }
    }

    public void logout(final User user) {
        synchronized (user) {
            user.clearPrivateAddresses();
            user.setPresence(User.Presence.Offline);
        }
    }

    public Collection<User> findAll() {
        return usersCache.values();
    }

    public User find(String username) {
        return usersCache.get(username);
    }

    public Map<String, User.Presence> getUserList() {
        final List<User> users = new LinkedList(findAll());

        final Map<String, User.Presence> userStates = new TreeMap<>();
        for (User user : users) {
            userStates.put(user.getUsername(), user.getPresence());
        }

        return userStates;
    }
}
