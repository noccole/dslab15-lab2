package service;

import entities.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public boolean login(User user, String password) {
        if (user.getPassword().equals(password)) {
            user.setPresence(User.Presence.Available);
            return true;
        } else {
            return false;
        }
    }

    public void logout(User user) {
        user.setPrivateAddress(null);
        user.setPresence(User.Presence.Offline);
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public User find(String username) {
        return users.get(username);
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
