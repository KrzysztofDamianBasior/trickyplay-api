package org.trickyplay.trickyplayapi.users.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.DeleteAccountResponse;
import org.trickyplay.trickyplayapi.users.dtos.EditAccountRequest;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AccountServiceUnitTest {
    private TPUserRepository tPUserRepository;
    private PasswordEncoder passwordEncoder;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        passwordEncoder = NoOpPasswordEncoder.getInstance();
        tPUserRepository = Mockito.mock(TPUserRepository.class);
        accountService = new AccountService(tPUserRepository, passwordEncoder);
    }

    // getAccount tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_getAccountIsCalled_then_returnTPUserRepresentation() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));
        TPUserRepresentation tPUserRepresentation = accountService.getAccount(tPUserStub.getId());
        assertThat(tPUserRepresentation.getId()).isEqualTo(tPUserStub.getId());
        assertThat(tPUserRepresentation.getName()).isEqualTo(tPUserStub.getName());
        assertThat(tPUserRepresentation.getRole()).isEqualTo(tPUserStub.getRole());
        assertThat(tPUserRepresentation.getCreatedAt()).isEqualTo(tPUserStub.getCreatedAt());
        assertThat(tPUserRepresentation.getUpdatedAt()).isEqualTo(tPUserStub.getUpdatedAt());
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_getAccountIsCalled_then_throwUserNotFoundException() {
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () ->
                accountService.getAccount(12345L)
        );
    }

    // deleteAccount tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_deleteAccountIsCalled_then_returnDeleteAccountResponse() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Mockito.doNothing().when(tPUserRepository).deleteById(tPUserStub.getId());

        DeleteAccountResponse deleteAccountResponse = accountService.deleteAccount(tPUserStub.getId());
        assertThat(deleteAccountResponse.getMessage()).isEqualTo("The account for user with id: " + tPUserStub.getId() + " has been removed");
    }

    // editAccount tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_editAccountIsCalled_then_returnCorrespondingTPUserRepresentation() {
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));
        Mockito.when(tPUserRepository.save(Mockito.any(TPUser.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    TPUser userArgument = i.getArgument(0, TPUser.class);
                    return TPUser.builder()
                            .id(userArgument.getId())
                            .password(userArgument.getPassword())
                            .name(userArgument.getName())
                            .role(userArgument.getRole())
                            .createdAt(userArgument.getCreatedAt())
                            .updatedAt(userArgument.getUpdatedAt())
                            .refreshTokens(userArgument.getRefreshTokens())
                            .build();
                });
        EditAccountRequest editAccountRequest = EditAccountRequest.builder()
                .newPassword("newPassword")
                .newUsername("newName")
                .build();
        TPUserRepresentation tPUserRepresentation = accountService.editAccount(tPUserStub.getId(), editAccountRequest);
        assertThat(tPUserRepresentation.getName()).isEqualTo(editAccountRequest.getNewUsername());
        assertThat(tPUserRepresentation.getId()).isEqualTo(tPUserStub.getId());
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_editAccountIsCalled_then_throwUserNotFoundException() {
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        EditAccountRequest editAccountRequest = EditAccountRequest.builder()
                .newPassword("newPassword")
                .newUsername("newName")
                .build();
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () ->
                accountService.editAccount(12345L, editAccountRequest)
        );
    }
}