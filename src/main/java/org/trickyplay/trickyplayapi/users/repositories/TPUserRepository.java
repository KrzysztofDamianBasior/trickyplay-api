package org.trickyplay.trickyplayapi.users.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.util.List;
import java.util.Optional;

public interface TPUserRepository extends JpaRepository<TPUser, Long> {
    Optional<TPUser> findByName(String name);

    Page<TPUser> findAll(Pageable page);

    List<TPUser> findAllByIdIn(List<Long> ids); // where x.id in ?1

    boolean existsById(Long id); // the exists projection in repository query derivation is supported since Spring Data JPA 1.11

    boolean existsByName(String name);

    // case expression was added in JPA 2.0
    // "SELECT u.name, CASE WHEN (u.id >= 100) THEN 1 WHEN (u.id < 100) THEN 2 ELSE 0 END FROM TPUser u"
    // emulate existence fun by using a COUNT query: The COUNT query works fine in this particular case since we are matching a UNIQUE column value. However, generally, for queries that return result sets having more than one record, we should prefer using EXISTS instead of COUNT
    // @Query(value = "select count(u.id) = 1 from TPUser u where u.name = :name")
    // @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM Users WHERE name = :name) THEN 'true' ELSE 'false' END ", nativeQuery = true)
    @Query("select case when count(u)> 0 then true else false end from TPUser u where lower(u.name) like lower(:name)")
    boolean existsByNameCustomQuery(@Param("name") String name);
}
