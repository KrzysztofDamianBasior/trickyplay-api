package org.trickyplay.trickyplayapi.users.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.enums.Role;

import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TPUserIntegrationTest extends BaseIntegrationTest {
    // TestEntityManager is auto provided and specifically designed to be used in JPA tests. Its methods like persistFlushFind are useful for setting up your data in a test. Otherwise, you would need to call multiple methods when using plain EntityManager. You can use TestEntityManager to insert some data and repository queries to get that same data and then make assertions.
    @Autowired
    private TestEntityManager entityManager; // TestEntityManager provides a subset of EntityManager methods that are useful for tests as well as helper methods for common testing tasks such as persist or find.

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = NoOpPasswordEncoder.getInstance();
    }

    @Test
    void twoUsersCanNotHaveTheSameName() {
        String username = "testUser";
        TPUser firstTPUser = TPUser.builder()
                .name(username)
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUser = TPUser.builder()
                .name(username)
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Long firstUserId = entityManager.persist(firstTPUser).getId();
        // when a JPA provider implementation encounters an error while communicating with the database server, it usually throws a generic PersistenceException except for SQLException. The SQL status code or error code of the SQLException may indicate a problem with the database server or a network problem. However, different databases have different SQL status codes and error codes for such problems, which makes it difficult to map the exception to a specific problem. Therefore, the exception mapping mechanism must be database specific
        // there are subclasses of PersistenceException: EntityExistsException, EntityNotFoundException, NonUniqueResultException, NoResultException, OptimisticLockException, RollbackException, TransactionRequiredException.
        assertThrows(PersistenceException.class, () -> entityManager.persist(secondTPUser));
    }
}