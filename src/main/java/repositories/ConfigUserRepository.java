package repositories;

import entities.User;
import util.Config;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * UserRepository implementation which loads users from the config file
 */
public class ConfigUserRepository implements UserRepository {
    private final Config config;

    private final static String USER_PASSWORD_KEY_ENDING = ".password";

    public ConfigUserRepository(Config config) {
        this.config = config;
    }

    @Override
    public Collection<User> findAll() throws RepositoryException {
        final Set<String> keys = config.listKeys();

        final Collection<User> users = new LinkedList<>();
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
