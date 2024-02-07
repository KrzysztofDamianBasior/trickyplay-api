package org.trickyplay.trickyplayapi.users.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

//@Transactional(propagation = Propagation.NOT_SUPPORTED) // If we want to disable transaction management
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest can configure databases, will scan for @Entity classes and configure Spring Data JPA repositories. It is also rollback at the end of each test. It provides access to a TestEntityManager bean- an alternative to the regular entity manager
class RefreshTokenRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void given_1User3RefreshTokensSavedInDB_when_findAllValidTokensByUserIsCalled_then_returnCorrespondingRefreshTokensList() {
        TPUser tPUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        RefreshToken firstRefreshToken = RefreshToken.builder()
                .token("firstRefreshToken")
                .owner(tPUser)
                .expiryDate(Instant.now().plusMillis(5000))
                .revoked(false)
                .build();

        RefreshToken secondRefreshToken = RefreshToken.builder()
                .token("secondRefreshToken")
                .owner(tPUser)
                .expiryDate(Instant.now().plusMillis(5000))
                .revoked(false)
                .build();

        RefreshToken thirdRefreshToken = RefreshToken.builder()
                .token("thirdRefreshToken")
                .owner(tPUser)
                .expiryDate(Instant.now().plusMillis(5000))
                .revoked(false)
                .build();

        Long userId = entityManager.persist(tPUser).getId();
        Long firstRefreshTokenId = entityManager.persist(firstRefreshToken).getId();
        Long secondRefreshTokenId = entityManager.persist(secondRefreshToken).getId();
        Long thirdRefreshTokenId = entityManager.persist(thirdRefreshToken).getId();
        List<RefreshToken> foundObjects = refreshTokenRepository.findAllValidTokensByUser(userId);

        assertThat(foundObjects)
                .hasSize(3)
                .hasSameElementsAs(List.of(firstRefreshToken, secondRefreshToken, thirdRefreshToken));
    }

    @Test
    void given_1User1RefreshTokenSavedInDB_when_findByTokenIsCalled_then_returnCorrespondingRefreshToken() {
        TPUser tPUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        String token = "test123";

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .owner(tPUser)
                .expiryDate(Instant.now().plusMillis(5000))
                .revoked(false)
                .build();

        Long userId = entityManager.persist(tPUser).getId();
        Long refreshTokenId = entityManager.persist(refreshToken).getId();

        Optional<RefreshToken> foundObject = refreshTokenRepository.findByToken(token);
        assertThat(foundObject).isPresent();
        assertThat(foundObject.get().token).isEqualTo(token);

        Optional<RefreshToken> unfoundedObject = refreshTokenRepository.findByToken("nonExistentToken");
        assertThat(unfoundedObject).isEmpty();
    }
}