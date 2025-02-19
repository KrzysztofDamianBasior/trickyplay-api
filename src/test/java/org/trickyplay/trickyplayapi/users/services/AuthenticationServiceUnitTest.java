package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.Mockito;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenExpiredOrRevokedException;
import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.*;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

class AuthenticationServiceUnitTest {
    private AuthenticationService authenticationService;
    private TPUserRepository tPUserRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;

//    static final String JWT_SECRET_KEY = "QbvYA50xFAP6QDIssCaAfWE5e5uT5v2m3ScMg6GcRad/lVat4rKwoLAkc2AJVN/z";
//    static final long JWT_EXPIRATION = 1000L;

    @BeforeEach
    void setUp() {
        passwordEncoder = NoOpPasswordEncoder.getInstance();

        tPUserRepository = Mockito.mock(TPUserRepository.class);
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);

//        jwtService = new JwtService(JWT_SECRET_KEY, JWT_EXPIRATION);
//        refreshTokenService = new RefreshTokenService(JWT_EXPIRATION, refreshTokenRepository, tPUserRepository);
        jwtService = Mockito.mock(JwtService.class);
        refreshTokenService = Mockito.mock(RefreshTokenService.class);

        authenticationService = new AuthenticationService(tPUserRepository, refreshTokenService, refreshTokenRepository, passwordEncoder, jwtService, authenticationManager);
    }

    // signIn tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_signInIsCalled_then_returnSignInResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        SignInRequest signInRequest = SignInRequest.builder()
                .password(userPassword)
                .username(userName)
                .build();
        TPUserPrincipal tPUserPrincipal = new TPUserPrincipal(tPUserStub);

        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()))).thenReturn(new UsernamePasswordAuthenticationToken(tPUserPrincipal, signInRequest.getPassword()));
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");
        Mockito.when(refreshTokenService.createAndSaveRefreshToken(Mockito.anyLong())).thenReturn(RefreshToken.builder()
                .id(1L)
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .owner(tPUserStub)
                .revoked(false)
                .token("token")
                .build()
        );

        SignInResponse signInResponse = authenticationService.signIn(signInRequest);

        assertThat(signInResponse.getAccessToken()).isNotBlank();
        assertThat(signInResponse.getRefreshToken()).isNotBlank();
        assertThat(signInResponse.getUserPublicInfo().getId()).isEqualTo(tPUserStub.getId());
        assertThat(signInResponse.getUserPublicInfo().getName()).isEqualTo(tPUserStub.getName());
    }

    @Test
    void given_1UserMockedInRepository_when_signInIsCalledWithInvalidPassword_then_throwBadCredentialsException() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        SignInRequest signInRequest = SignInRequest.builder()
                .password("differentPassword")
                .username(userName)
                .build();

        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()))).thenThrow(BadCredentialsException.class);
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");
        Mockito.when(refreshTokenService.createAndSaveRefreshToken(Mockito.anyLong())).thenReturn(RefreshToken.builder()
                .id(1L)
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .owner(tPUserStub)
                .revoked(false)
                .token("token")
                .build()
        );

        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(signInRequest)); // AuthenticationException
    }

    @Test
    void given_1UserMockedInRepository_when_signInIsCalledWithAbsentName_then_throwBadCredentialsException() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        SignInRequest signInRequest = SignInRequest.builder()
                .password(userPassword)
                .username("usernameNotPresentInTheDatabase")
                .build();

        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()))).thenThrow(BadCredentialsException.class);
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");
        Mockito.when(refreshTokenService.createAndSaveRefreshToken(Mockito.anyLong())).thenReturn(RefreshToken.builder()
                .id(1L)
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .owner(tPUserStub)
                .revoked(false)
                .token("token")
                .build()
        );

        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(signInRequest)); // AuthenticationException
    }

    // singleSessionLogout tests -----------------------------------------------
    @Test
    void given_1User1RefreshTokenMockedInRepository_when_singleSessionLogoutIsCalledWithValidRefreshToken_then_returnSignOutResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();

        Mockito.when(refreshTokenRepository.findByToken(refreshTokenStub.getToken())).thenReturn(Optional.of(refreshTokenStub));
        Mockito.when(refreshTokenRepository.save(Mockito.any(RefreshToken.class)))
                .thenAnswer(i -> {
                    // get the first argument passed, in this case we only have one parameter, and it is on place 0
                    return i.getArgument(0, RefreshToken.class);
                });

        // when
        SignOutResponse signOutResponse = authenticationService.singleSessionLogout(refreshTokenStub.getToken());

        // then
        assertThat(signOutResponse.getNumberOfRefreshTokensRemoved()).isEqualTo(1);
    }

    @Test
    void given_1User1RefreshTokenMockedInRepository_when_singleSessionLogoutIsCalledWithInvalidRefreshToken_then_returnSignOutResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("token")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        String absentToken = "absent-token";
        Mockito.when(refreshTokenRepository.findByToken(absentToken)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenNotFoundException.class, () -> authenticationService.singleSessionLogout("invalid-token"));
    }

    // allSessionsLogout tests -----------------------------------------------
    @Test
    void given_1User3RefreshTokensMockedInRepository_when_allSessionsLogoutIsCalled_then_returnSignOutResponse() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .id(2L)
                .owner(tPUserStub)
                .revoked(false)
                .token("token2")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken thirdRefreshTokenStub = RefreshToken.builder()
                .id(3L)
                .owner(tPUserStub)
                .revoked(false)
                .token("token3")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.of(tPUserStub));
        Mockito.when(refreshTokenService.revokeAllUserTokens(tPUserStub)).thenReturn(3);

        // when
        SignOutResponse signOutResponse = authenticationService.allSessionsLogout(tPUserStub.getId());

        // then
        assertThat(signOutResponse.getNumberOfRefreshTokensRemoved()).isEqualTo(3);
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_allSessionsLogoutIsCalled_then_throwUserNotFoundException() {
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () ->  authenticationService.allSessionsLogout(12345L));
    }

    // signUp tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_signUpIsCalled_then_returnSignInResponse() {
        String userPassword = "123TestUserPassword";
        String userName = "user";
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name(userName)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(tPUserStub.getName())
                .password(tPUserStub.getPassword())
                .build();
        Mockito.when(tPUserRepository.save(Mockito.any(TPUser.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    return i.getArgument(0, TPUser.class);
                });
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");
        Mockito.when(refreshTokenService.createAndSaveRefreshToken(Mockito.any(TPUser.class))).thenAnswer(i -> {
            TPUser tokenOwner = i.getArgument(0, TPUser.class);
            return RefreshToken.builder()
                    .id(1L)
                    .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                    .owner(tokenOwner)
                    .revoked(false)
                    .token("token")
                    .build();
            }
        );

        SignInResponse signInResponse = authenticationService.signUp(signUpRequest);

        assertThat(signInResponse.getRefreshToken()).isNotBlank();
        assertThat(signInResponse.getAccessToken()).isNotBlank();
        assertThat(signInResponse.getUserPublicInfo().getName()).isEqualTo(userName);
    }

    // refreshAccessToken tests -----------------------------------------------
    @Test
    void given_1User1RefreshTokenMockedInRepository_when_refreshAccessTokenIsCalled_then_returnRefreshTokenResponse() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserStub)
                .revoked(false)
                .token("token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        Mockito.when(refreshTokenService.findByToken(refreshTokenStub.getToken())).thenReturn(Optional.of(refreshTokenStub));
        Mockito.when(refreshTokenService.verifyIfTokenExpiredOrRevoked(Mockito.any(RefreshToken.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    RefreshToken refreshToken = i.getArgument(0, RefreshToken.class);
                    return refreshToken.getExpiryDate().compareTo(Instant.now()) < 0 || refreshToken.isRevoked();
                });
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");

        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(refreshTokenStub.getToken()).build();

        RefreshTokenResponse refreshTokenResponse = authenticationService.refreshAccessToken(refreshTokenRequest);

        assertThat(refreshTokenResponse.getAccessToken()).isNotBlank();
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_refreshAccessTokenIsCalled_then_throwRefreshTokenNotFoundException() {
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken("12345").build();
        assertThrows(RefreshTokenNotFoundException.class, () ->  authenticationService.refreshAccessToken(refreshTokenRequest));
    }

    @Test
    void given_1User1RefreshTokenMockedInRepository_when_refreshAccessTokenIsCalledWithRevokedToken_then_throwRefreshTokenExpiredOrRevokedException() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .role(Role.USER)
                .password(passwordEncoder.encode("123TestUserPassword"))
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .owner(tPUserStub)
                .revoked(true)
                .token("token1")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        Mockito.when(refreshTokenService.findByToken(refreshTokenStub.getToken())).thenReturn(Optional.of(refreshTokenStub));
        Mockito.when(refreshTokenService.verifyIfTokenExpiredOrRevoked(Mockito.any(RefreshToken.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    RefreshToken refreshToken = i.getArgument(0, RefreshToken.class);
                    return refreshToken.getExpiryDate().compareTo(Instant.now()) < 0 || refreshToken.isRevoked();
                });
        Mockito.when(jwtService.issueToken(Mockito.any())).thenReturn("token");

        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(refreshTokenStub.getToken()).build();

        assertThrows(RefreshTokenExpiredOrRevokedException.class, () ->  authenticationService.refreshAccessToken(refreshTokenRequest));
    }
}