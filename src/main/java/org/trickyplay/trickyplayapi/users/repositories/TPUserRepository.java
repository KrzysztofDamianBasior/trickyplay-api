package org.trickyplay.trickyplayapi.users.repositories;

import org.trickyplay.trickyplayapi.users.entities.TPUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TPUserRepository extends JpaRepository<TPUser, Long> {
    Optional<TPUser> findByName(String username);
}
