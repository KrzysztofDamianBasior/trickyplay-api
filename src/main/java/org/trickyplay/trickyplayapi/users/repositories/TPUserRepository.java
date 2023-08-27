package org.trickyplay.trickyplayapi.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.util.Optional;

public interface TPUserRepository extends JpaRepository<TPUser, Integer> {
    Optional<TPUser> findByName(String username);
}
