package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.DeleteAccountResponse;
import org.trickyplay.trickyplayapi.users.dtos.EditAccountRequest;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
class AccountServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TPUserRepository tPUserRepository;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountService accountService;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_getAccountIsCalled_then_returnTPUserRepresentation() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        TPUserRepresentation tPUserRepresentation = accountService.getAccount(tPUserSaved.getId());

        assertThat(tPUserRepresentation.getId()).isEqualTo(tPUserSaved.getId());
        assertThat(tPUserRepresentation.getName()).isEqualTo(tPUserSaved.getName());
        assertThat(tPUserRepresentation.getRole()).isEqualTo(tPUserSaved.getRole());
        assertThat(tPUserRepresentation.getCreatedAt()).isEqualTo(tPUserSaved.getCreatedAt());
        assertThat(tPUserRepresentation.getUpdatedAt()).isEqualTo(tPUserSaved.getUpdatedAt());
    }

    @Test
    @Transactional
    void given_emptyDB_when_getAccountIsCalled_then_throwUserNotFoundException() {
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () ->
                accountService.getAccount(12345L)
        );
    }

    // deleteAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_deleteAccountIsCalled_then_returnDeleteAccountResponse() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        DeleteAccountResponse deleteAccountResponse = accountService.deleteAccount(tPUserSaved.getId());
        assertThat(deleteAccountResponse.getMessage()).isEqualTo("The account for user with id: " + tPUserSaved.getId() + " has been removed");
    }

    // editAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_editAccountIsCalled_then_returnCorrespondingTPUserRepresentation() {
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        EditAccountRequest editAccountRequest = EditAccountRequest.builder()
                .newPassword("newPassword")
                .newUsername("newName")
                .build();
        TPUserRepresentation tPUserRepresentation = accountService.editAccount(tPUserSaved.getId(), editAccountRequest);

        assertThat(tPUserRepresentation.getName()).isEqualTo(editAccountRequest.getNewUsername());
        assertThat(tPUserRepresentation.getId()).isEqualTo(tPUserSaved.getId());
    }

    @Test
    @Transactional
    void given_emptyDB_when_editAccountIsCalled_then_throwUserNotFoundException() {
        EditAccountRequest editAccountRequest = EditAccountRequest.builder()
                .newPassword("newPassword")
                .newUsername("newName")
                .build();

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () ->
                accountService.editAccount(12345L, editAccountRequest)
        );
    }
}