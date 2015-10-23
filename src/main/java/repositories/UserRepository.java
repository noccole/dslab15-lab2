package repositories;

import entities.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll() throws RepositoryException;
}
