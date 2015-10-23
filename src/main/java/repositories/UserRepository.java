package repositories;

import entities.User;

import java.util.Collection;

public interface UserRepository {
    Collection<User> findAll() throws RepositoryException;
}
