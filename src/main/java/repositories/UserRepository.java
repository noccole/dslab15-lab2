package repositories;

import entities.User;

import java.util.Collection;

public interface UserRepository {
    /**
     * Find all existing users
     *
     * @return A collection of all existing users
     * @throws RepositoryException
     */
    Collection<User> findAll() throws RepositoryException;
}
