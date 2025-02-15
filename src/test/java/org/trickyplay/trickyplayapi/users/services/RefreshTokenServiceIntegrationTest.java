package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
@TestPropertySource(properties = {
        "application.security.jwt.refresh-token-expiration=1000000",
})
class RefreshTokenServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TPUserRepository tPUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // createAndSaveRefreshToken tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_createAndSaveRefreshTokenWithUserIdIsCalled_than_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        RefreshToken result = refreshTokenService.createAndSaveRefreshToken(tPUserSaved.getId());

        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserStub);
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_createAndSaveRefreshTokenWithUserEntityIsCalled_than_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        RefreshToken result = refreshTokenService.createAndSaveRefreshToken(tPUserSaved);

        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserSaved);
    }

    // findByToken tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_findByTokenIsCalled_then_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);

        RefreshToken result = refreshTokenService.findByToken(refreshTokenStub.getToken()).get();

        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserSaved);
    }

    // deleteTokenIfExpired tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_deleteTokenIfExpiredIsCalled_then_returnCorrespondingBooleanValue() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);

        Boolean result = refreshTokenService.deleteTokenIfExpired(refreshTokenSaved);

        assertThat(result).isTrue();
    }

    // revokeAllUserTokens tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3RefreshTokenSavedInDB_when_revokeAllUserTokensIsCalled_then_returnNumberOfRevokedTokens() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token("token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token("token2")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken thirdRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token("token3")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken firstRefreshTokenSaved = refreshTokenRepository.save(firstRefreshTokenStub);
        RefreshToken secondRefreshTokenSaved = refreshTokenRepository.save(secondRefreshTokenStub);
        RefreshToken thirdRefreshTokenSaved = refreshTokenRepository.save(thirdRefreshTokenStub);

        int numOfRevokedTokens = refreshTokenService.revokeAllUserTokens(tPUserSaved);

        assertThat(numOfRevokedTokens).isEqualTo(3);
    }
}