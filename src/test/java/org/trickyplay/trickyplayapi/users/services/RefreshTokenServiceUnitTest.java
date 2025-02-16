package org.trickyplay.trickyplayapi.users.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

// @ExtendWith(MockitoExtension.class)
class RefreshTokenServiceUnitTest {
    private TPUserRepository tPUserRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        tPUserRepository = Mockito.mock(TPUserRepository.class);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, tPUserRepository, 5000L);
        // MockitoAnnotations.initMocks(this); // to enable Mockito annotations during test executions this static method has to be called, another way to enable Mockito annotations is annotating the test class with @RunWith by specifying the MockitoJUnitRunner that does this task and also other useful things
    }

    // createAndSaveRefreshToken tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_createAndSaveRefreshTokenWithUserIdIsCalled_than_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        Mockito.when(tPUserRepository.getReferenceById(tPUserStub.getId())).thenReturn(tPUserStub);
        Mockito.when(refreshTokenRepository.save(Mockito.any(RefreshToken.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    RefreshToken refreshTokenArgument = i.getArgument(0, RefreshToken.class);
                    return RefreshToken.builder()
                            .id(refreshTokenArgument.getId())
                            .token(refreshTokenArgument.getToken())
                            .expiryDate(refreshTokenArgument.getExpiryDate())
                            .revoked(refreshTokenArgument.revoked)
                            .owner(refreshTokenArgument.getOwner())
                            .build();
                });

        RefreshToken result = refreshTokenService.createAndSaveRefreshToken(tPUserStub.getId());

        Mockito.verify(tPUserRepository).getReferenceById(tPUserStub.getId());
        Mockito.verifyNoMoreInteractions(tPUserRepository);
        Mockito.verify(refreshTokenRepository).save(Mockito.any(RefreshToken.class));
        Mockito.verifyNoMoreInteractions(refreshTokenRepository);
        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserStub);
    }

    @Test
    void given_1UserMockedInRepository_when_createAndSaveRefreshTokenWithUserEntityIsCalled_than_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        Mockito.when(refreshTokenRepository.save(Mockito.any(RefreshToken.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    RefreshToken refreshTokenArgument = i.getArgument(0, RefreshToken.class);
                    return RefreshToken.builder()
                            .id(refreshTokenArgument.getId())
                            .token(refreshTokenArgument.getToken())
                            .expiryDate(refreshTokenArgument.getExpiryDate())
                            .revoked(refreshTokenArgument.revoked)
                            .owner(refreshTokenArgument.getOwner())
                            .build();
                });

        RefreshToken result = refreshTokenService.createAndSaveRefreshToken(tPUserStub);

        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserStub);
    }

    // findByToken tests -----------------------------------------------
    @Test
    void given_1User1RefreshTokenMockedInRepository_when_findByTokenIsCalled_then_returnCorrespondingRefreshToken() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        Mockito.when(refreshTokenRepository.findByToken(refreshTokenStub.getToken())).thenReturn(Optional.of(refreshTokenStub));

        RefreshToken result = refreshTokenService.findByToken(refreshTokenStub.getToken()).get();

        assertThat(result.revoked).isFalse();
        assertThat(result.getOwner()).isEqualTo(tPUserStub);
    }

    // deleteTokenIfExpired tests -----------------------------------------------
    @Test
    void given_1User1RefreshTokenMockedInRepository_when_deleteTokenIfExpiredIsCalled_then_returnCorrespondingBooleanValue() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        Mockito.doNothing().when(refreshTokenRepository).delete(Mockito.any());

        Boolean result = refreshTokenService.deleteTokenIfExpired(refreshTokenStub);

        assertThat(result).isTrue();
    }

    // verifyIfTokenExpiredOrRevoked tests -----------------------------------------------
    @Test
    void given_1User2RefreshTokensMockedInRepository_when_verifyIfTokenExpiredOrRevokedIsCalled_then_returnCorrespondingBooleanValue() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(true)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .id(2L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();

        Boolean firstResult = refreshTokenService.verifyIfTokenExpiredOrRevoked(firstRefreshTokenStub);
        Boolean secondResult = refreshTokenService.verifyIfTokenExpiredOrRevoked(secondRefreshTokenStub);

        assertThat(firstResult).isTrue();
        assertThat(secondResult).isTrue();
    }

    // revokeAllUserTokens tests -----------------------------------------------
    @Test
    void given_1User3RefreshTokenMockedInRepository_when_revokeAllUserTokensIsCalled_then_returnNumberOfRevokedTokens() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .role(Role.USER)
                .password("password1234PASSWORD")
                .createdAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .updatedAt(LocalDateTime.parse("2018-12-30T19:34:50.63"))
                .build();
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .id(1L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("3000-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .id(2L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        RefreshToken thirdRefreshTokenStub = RefreshToken.builder()
                .id(3L)
                .owner(tPUserStub)
                .revoked(false)
                .token("asdf")
                .expiryDate((LocalDateTime.parse("2018-12-30T19:34:50.63").toInstant(ZoneOffset.UTC)))
                .build();
        tPUserStub.setRefreshTokens(List.of(firstRefreshTokenStub, secondRefreshTokenStub, thirdRefreshTokenStub));
        Mockito.when(refreshTokenRepository.findAllValidTokensByUser(tPUserStub.getId())).thenReturn(tPUserStub.getRefreshTokens());
        Mockito.when(refreshTokenRepository.saveAll(Mockito.anyList())).thenReturn(tPUserStub.getRefreshTokens());

        int numOfRevokedTokens = refreshTokenService.revokeAllUserTokens(tPUserStub);

        assertThat(numOfRevokedTokens).isEqualTo(3);
    }
}