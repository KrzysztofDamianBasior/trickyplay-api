package org.trickyplay.trickyplayapi.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenExpiredOrRevokedException;
import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.*;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

@SpringBootTest
class AuthenticationServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TPUserRepository tPUserRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // signIn tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_signInIsCalledWithValidPassword_then_returnSignInResponse() {
        // given
        String userPassword = "123TestUserPassword";
        String name = "signIn1";
        TPUser tPUserStub = TPUser.builder()
                .name(name)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        SignInRequest signInRequest = SignInRequest.builder()
                .password(userPassword)
                .username(name)
                .build();

        // when
        SignInResponse signInResponse = authenticationService.signIn(signInRequest);

        // then
        assertThat(signInResponse.getAccessToken()).isNotBlank();
        assertThat(signInResponse.getRefreshToken()).isNotBlank();
        assertThat(signInResponse.getUserPublicInfo().getId()).isEqualTo(tPUserSaved.getId());
        assertThat(signInResponse.getUserPublicInfo().getName()).isEqualTo(tPUserSaved.getName());
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_signInIsCalledWithInvalidPassword_then_throwBadCredentialsException() {
        String userPassword = "123TestUserPassword";
        String userName = "signIn2";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        SignInRequest signInRequest = SignInRequest.builder()
                .password("differentPassword")
                .username(userName)
                .build();

        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(signInRequest)); // AuthenticationException
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_signInIsCalledWithAbsentName_then_throwBadCredentialsException() {
        String userPassword = "123TestUserPassword";
        String userName = "signIn3";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        SignInRequest signInRequest = SignInRequest.builder()
                .password(userPassword)
                .username("usernameNotPresentInTheDB")
                .build();

        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(signInRequest)); // AuthenticationException
    }

    // singleSessionLogout tests -----------------------------------------------
    @Test
    void given_1User1RefreshTokenSavedInDB_when_singleSessionLogoutIsCalledWithValidRefreshToken_then_returnSignOutResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "ssLogout1";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);

        // when
        SignOutResponse signOutResponse = authenticationService.singleSessionLogout(refreshTokenSaved.getToken());

        // then
        assertThat(signOutResponse.getNumberOfRefreshTokensRemoved()).isEqualTo(1);
    }

    @Test
    void given_1User1RefreshTokenSavedInDB_when_singleSessionLogoutIsCalledWithInvalidRefreshToken_then_returnSignOutResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "ssLogout2";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);

        assertThrows(RefreshTokenNotFoundException.class, () -> authenticationService.singleSessionLogout("invalid-token"));
    }

    // allSessionsLogout tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3RefreshTokensSavedInDB_when_allSessionsLogoutIsCalled_then_returnSignOutResponse() {
        String userName = "asLogout";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token2")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken thirdRefreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token3")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken firstRefreshTokenSaved = refreshTokenRepository.save(firstRefreshTokenStub);
        RefreshToken secondRefreshTokenSaved = refreshTokenRepository.save(secondRefreshTokenStub);
        RefreshToken thirdRefreshTokenSaved = refreshTokenRepository.save(thirdRefreshTokenStub);

        // when
        SignOutResponse signOutResponse = authenticationService.allSessionsLogout(tPUserSaved.getId());

        // then
        assertThat(signOutResponse.getNumberOfRefreshTokensRemoved()).isEqualTo(3);
    }

    @Test
    @Transactional
    void given_emptyDB_when_allSessionsLogoutIsCalled_then_throwUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () ->  authenticationService.allSessionsLogout(12345L));
    }

    // signUp tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_signUpIsCalled_then_returnSignInResponse() {
        String username = "signUp";
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(username)
                .password("password")
                .build();
        SignInResponse signInResponse = authenticationService.signUp(signUpRequest);
        assertThat(signInResponse.getRefreshToken()).isNotBlank();
        assertThat(signInResponse.getAccessToken()).isNotBlank();
        assertThat(signInResponse.getUserPublicInfo().getName()).isEqualTo(username);
    }

    // refreshAccessToken tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_refreshAccessTokenIsCalled_then_returnRefreshTokenResponse() {
        String userName = "refAccessT1";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(false)
                .token(userName+"Token")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(refreshTokenSaved.getToken()).build();

        RefreshTokenResponse refreshTokenResponse = authenticationService.refreshAccessToken(refreshTokenRequest);

        assertThat(refreshTokenResponse.getAccessToken()).isNotBlank();
    }

    @Test
    @Transactional
    void given_emptyDB_when_refreshAccessTokenIsCalled_then_throwRefreshTokenNotFoundException() {
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken("12345").build();
        assertThrows(RefreshTokenNotFoundException.class, () ->  authenticationService.refreshAccessToken(refreshTokenRequest));
    }

    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_refreshAccessTokenIsCalledWithRevokedToken_then_throwRefreshTokenExpiredOrRevokedException() {
        String userName = "refAccessT2";
        TPUser tPUserStub = TPUser.builder()
                .name(userName)
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserSaved)
                .revoked(true)
                .token(userName + "Token")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken refreshTokenSaved = refreshTokenRepository.save(refreshTokenStub);

        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(refreshTokenSaved.getToken()).build();

        assertThrows(RefreshTokenExpiredOrRevokedException.class, () ->  authenticationService.refreshAccessToken(refreshTokenRequest));
    }
}