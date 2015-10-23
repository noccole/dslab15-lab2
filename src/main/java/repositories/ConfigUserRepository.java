package repositories;

import entities.User;
import util.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConfigUserRepository implements UserRepository {
    private final Config config;

    private final static String USER_PASSWORD_KEY_ENDING = ".password";

    public ConfigUserRepository(Config config) {
        this.config = config;
    }

    @Override
    public List<User> findAll() throws RepositoryException {
        final Set<String> keys = config.listKeys();

        final List<User> users = new LinkedList<>();
        for (String key : keys) {
            if (key.endsWith(USER_PASSWORD_KEY_ENDING)) {
                final String username = key.substring(0, key.length() - USER_PASSWORD_KEY_ENDING.length());

                final User user = new User(username);
                user.setPassword(config.getString(key));
                users.add(user);
            }
        }

        return users;
    }
}
