package org.trickyplay.trickyplayapi.users.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

//@Transactional(propagation = Propagation.NOT_SUPPORTED) // If we want to disable transaction management
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest can configure databases, will scan for @Entity classes and configure Spring Data JPA repositories. It is also rollback at the end of each test. It provides access to a TestEntityManager bean- an alternative to the regular entity manager
class TPUserRepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TPUserRepository userRepository;

    // TestEntityManager is auto provided and specifically designed to be used in JPA tests. Its methods like persistFlushFind are useful for setting up your data in a test. Otherwise, you would need to call multiple methods when using plain EntityManager. You can use TestEntityManager to insert some data and repository queries to get that same data and then make assertions.
    @Autowired
    private TestEntityManager entityManager; // TestEntityManager provides a subset of EntityManager methods that are useful for tests as well as helper methods for common testing tasks such as persist or find.

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void given_1UserSavedInDB_when_findByNameIsCalled_then_returnCorrespondingTPUser() {
        TPUser tPUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Long userId = entityManager.persist(tPUser).getId();

        Optional<TPUser> foundUser = userRepository.findByName(tPUser.getName());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(tPUser.getId());
    }

    @Test
    void given_3UsersSavedInDB_when_findAllIsCalled_then_returnCorrespondingTPUsersPage() {
        TPUser firstUser = TPUser.builder()
                .name("firstUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondUser = TPUser.builder()
                .name("secondUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser thirdUser = TPUser.builder()
                .name("thirdUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Long firstUserId = entityManager.persist(firstUser).getId();
        Long secondUserId = entityManager.persist(secondUser).getId();
        Long thirdUserId = entityManager.persist(thirdUser).getId();

        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");
        Page<TPUser> usersPage = userRepository.findAll(pageable);

        assertThat(usersPage.getContent())
                .contains(firstUser)
                .contains(secondUser)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .hasSameElementsAs(List.of(firstUser, secondUser));
        assertThat(usersPage.getTotalPages()).isEqualTo(2);
        assertThat(usersPage.getTotalElements()).isEqualTo(3);
        assertThat(usersPage.isLast()).isFalse();
    }

    @Test
    void given_3UsersSavedInDB_when_findAllByIdInIsCalled_then_returnCorrespondingTPUsersList() {
        TPUser firstUser = TPUser.builder()
                .name("firstUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondUser = TPUser.builder()
                .name("secondUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser thirdUser = TPUser.builder()
                .name("thirdUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Long firstUserId = entityManager.persist(firstUser).getId();
        Long secondUserId = entityManager.persist(secondUser).getId();
        Long thirdUserId = entityManager.persist(thirdUser).getId();

        List<TPUser> usersList = userRepository.findAllByIdIn(List.of(firstUserId, secondUserId, thirdUserId));

        assertThat(usersList)
                .contains(firstUser)
                .contains(secondUser)
                .contains(thirdUser)
                .hasSize(3)
                .doesNotHaveDuplicates()
                .hasSameElementsAs(List.of(firstUser, secondUser, thirdUser));
    }

    @Test
    void given_1UserSavedInDB_when_existsByIdIsCalled_then_returnCorrespondingBooleanValue() {
        TPUser tPUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Long userId = entityManager.persist(tPUser).getId();

        boolean doesExists = userRepository.existsById(userId);
        assertThat(doesExists).isTrue();

        boolean doesNotExists = userRepository.existsById(12345L);
        assertThat(doesNotExists).isFalse();
    }

    @Test
    void given_1UserSavedInDB_when_existsByNameIsCalled_then_returnCorrespondingBooleanValue() {
        String userName = "testUser";
        TPUser tPUser = TPUser.builder()
                .name(userName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Long userId = entityManager.persist(tPUser).getId();

        boolean doesExists = userRepository.existsByName(userName);
        assertThat(doesExists).isTrue();

        boolean doesNotExists = userRepository.existsByName("nonExistentUsername");
        assertThat(doesNotExists).isFalse();
    }

    @Test
    void given_1UserSavedInDB_when_existsByNameCustomQueryIsCalled_then_returnCorrespondingBooleanValue() {
        String userName = "testUser";
        TPUser tPUser = TPUser.builder()
                .name(userName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Long userId = entityManager.persist(tPUser).getId();

        boolean doesExists = userRepository.existsByNameCustomQuery(userName);
        assertThat(doesExists).isTrue();

        boolean doesNotExists = userRepository.existsByNameCustomQuery("nonExistentUsername");
        assertThat(doesNotExists).isFalse();
    }
}