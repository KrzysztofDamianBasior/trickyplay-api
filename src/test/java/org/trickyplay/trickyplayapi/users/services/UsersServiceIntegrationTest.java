package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.GetUsersResponse;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.records.UsersPageArgs;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
class UsersServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TPUserRepository tPUserRepository;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getUsers tests -----------------------------------------------
    @Test
    @Transactional
    void given_3UsersSavedInDB_when_getUsersIsCalled_then_returnCorrespondingGetUsersResponse() {
        // given
        TPUser firstTPUserStub = TPUser.builder()
                .name("firstUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUserStub = TPUser.builder()
                .name("secondUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser thirdTPUserStub = TPUser.builder()
                .name("thirdUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser firstTPUserSaved = tPUserRepository.save(firstTPUserStub);
        TPUser secondTPUserSaved = tPUserRepository.save(secondTPUserStub);
        TPUser thirdTPUserSaved = tPUserRepository.save(thirdTPUserStub);

        // when
        UsersPageArgs usersPageArgs = new UsersPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetUsersResponse getUsersResponse = usersService.getUsers(usersPageArgs);

        // then
        assertThat(getUsersResponse.getUsers())
                .hasSize(3)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(getUsersResponse.isLast()).isTrue();
        assertThat(getUsersResponse.getTotalElements()).isEqualTo(3);
        assertThat(getUsersResponse.getTotalPages()).isEqualTo(1);
        assertThat(getUsersResponse.getPageSize()).isEqualTo(10);
        assertThat(getUsersResponse.getUsers().get(0).getId()).isEqualTo(firstTPUserSaved.getId());
    }

    // getUser tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_getUserIsCalledWithSavedUserId_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        TPUserRepresentation userRepresentation = usersService.getUser(tPUserSaved.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserSaved.getId());
    }

    @Test
    @Transactional
    void given_emptyDB_when_getUserIsCalledWithIdNotPresentInDB_then_throwUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> usersService.getUser(1000L));
    }

    // grantAdminPermissions tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_grantAdminPermissionsIsCalledOnThisUser_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        TPUserRepresentation userRepresentation = usersService.grantAdminPermissions(tPUserSaved.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserSaved.getId());
        assertThat(userRepresentation.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @Transactional
    void given_1AdminSavedInDB_when_grantAdminPermissionsIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        TPUser tPUserStub = TPUser.builder()
                .name("admin")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.grantAdminPermissions(tPUserSaved.getId()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_grantAdminPermissionsIsCalled_then_throwUserNotFoundException() {
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.grantAdminPermissions(1L));
    }

    // banUser tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_banUserIsCalledOnThatUser_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        TPUserRepresentation userRepresentation = usersService.banUser(tPUserStub.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserStub.getId());
        assertThat(userRepresentation.getRole()).isEqualTo(Role.BANNED);
    }

    @Test
    @Transactional
    void given_1AdminSavedInDB_when_banUserIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.banUser(tPUserSaved.getId()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_banUserIsCalled_then_throwUserNotFoundException() {
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.banUser(1L));
    }

    // unbanUser tests -----------------------------------------------
    @Test
    @Transactional
    void given_1BannedUserSavedInDB_when_unbanUserIsCalledOnBanned_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.BANNED)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        TPUserRepresentation userRepresentation = usersService.unbanUser(tPUserSaved.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserSaved.getId());
    }

    @Test
    @Transactional
    void given_1AdminSavedInDB_when_unbanUserIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.unbanUser(tPUserSaved.getId()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_unbanUserIsCalled_then_throwUserNotFoundException() {
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.unbanUser(1L));
    }

    // checkIfUserExistsById tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_checkIfUserExistsByIdIsCalled_then_returnProperBoolean() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        Boolean doesExist = usersService.checkIfUserExistsById(tPUserSaved.getId());

        // then
        assertThat(doesExist).isTrue();
    }

    // checkIfUserExistsByName tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_grantAdminPermissionsIsCalledByAdmin_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        Boolean doesExist = usersService.checkIfUserExistsByName(tPUserSaved.getName());

        // then
        assertThat(doesExist).isTrue();
    }
}