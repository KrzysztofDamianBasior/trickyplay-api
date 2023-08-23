package org.trickyplay.trickyplayapi.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trickyplay.trickyplayapi.users.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String username);
}
