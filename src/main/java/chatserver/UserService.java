package chatserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

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
}
