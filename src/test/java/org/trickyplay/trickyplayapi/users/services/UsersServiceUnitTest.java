package org.trickyplay.trickyplayapi.users.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

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
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsersServiceUnitTest {
    @Mock
    private TPUserRepository tPUserRepository;
    @InjectMocks
    private UsersService usersService;

    // getUsers tests -----------------------------------------------
    @Test
    void given_3UsersMockedInRepository_when_getUsersIsCalled_then_returnCorrespondingGetUsersResponse() {
        // given
        TPUser firstTPUserStub = TPUser.builder()
                .id(1L)
                .name("firstUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUserStub = TPUser.builder()
                .id(2L)
                .name("secondUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser thirdTPUserStub = TPUser.builder()
                .id(3L)
                .name("thirdUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        List<TPUser> users = List.of(firstTPUserStub, secondTPUserStub, thirdTPUserStub);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<TPUser> usersPageStub = new PageImpl<>(users, page0Size10, users.size());

        Mockito.when(tPUserRepository.findAll(PageRequest.of(0, 10, Sort.Direction.ASC, "id")))
                .thenReturn(usersPageStub);

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
        assertThat(getUsersResponse.getUsers().get(0).getId()).isEqualTo(firstTPUserStub.getId());
    }

    // getUser tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_getUserIsCalled_then_returnCorrespondingUserRepresentation() {
        // given
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

        // when
        TPUserRepresentation userRepresentation = usersService.getUser(tPUserStub.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserStub.getId());
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_getUserIsCalled_then_throwUserNotFoundException() {
        // given
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when,then
        assertThrows(UserNotFoundException.class, () -> usersService.getUser(1000L));
    }

    // grantAdminPermissions tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_grantAdminPermissionsIsCalledOnUser_then_returnCorrespondingUserRepresentation() {
        // given
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
                            .role(userArgument.getRole())
                            .createdAt(userArgument.getCreatedAt())
                            .updatedAt(userArgument.getUpdatedAt())
                            .refreshTokens(userArgument.getRefreshTokens())
                            .build();
                });

        // when
        TPUserRepresentation userRepresentation = usersService.grantAdminPermissions(tPUserStub.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserStub.getId());
        assertThat(userRepresentation.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void given_1UserMockedInRepository_when_grantAdminPermissionsIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.grantAdminPermissions(tPUserStub.getId()));
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_grantAdminPermissionsIsCalled_then_throwUserNotFoundException() {
        // given
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.grantAdminPermissions(1L));
    }

    // banUser tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_banUserIsCalledOnUser_then_returnCorrespondingUserRepresentation() {
        // given
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
                            .role(userArgument.getRole())
                            .createdAt(userArgument.getCreatedAt())
                            .updatedAt(userArgument.getUpdatedAt())
                            .refreshTokens(userArgument.getRefreshTokens())
                            .build();
                });

        // when
        TPUserRepresentation userRepresentation = usersService.banUser(tPUserStub.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserStub.getId());
        assertThat(userRepresentation.getRole()).isEqualTo(Role.BANNED);
    }

    @Test
    void given_1UserMockedInRepository_when_banUserIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.banUser(tPUserStub.getId()));
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_banUserIsCalled_then_throwUserNotFoundException() {
        // given
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.banUser(1L));
    }

    // unbanUser tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_unbanUserIsCalledOnBanned_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.BANNED)
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
                            .role(userArgument.getRole())
                            .createdAt(userArgument.getCreatedAt())
                            .updatedAt(userArgument.getUpdatedAt())
                            .refreshTokens(userArgument.getRefreshTokens())
                            .build();
                });

        // when
        TPUserRepresentation userRepresentation = usersService.unbanUser(tPUserStub.getId());

        // then
        assertThat(userRepresentation.getId()).isEqualTo(tPUserStub.getId());
    }

    @Test
    void given_1UserMockedInRepository_when_unbanUserIsCalledOnAdmin_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.ADMIN)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> usersService.unbanUser(tPUserStub.getId()));
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_unbanUserIsCalled_then_throwUserNotFoundException() {
        // given
        Mockito.when(tPUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> usersService.unbanUser(1L));
    }

    // checkIfUserExistsById tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_checkIfUserExistsByIdIsCalled_then_returnProperBoolean() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Mockito.when(tPUserRepository.existsById(tPUserStub.getId())).thenReturn(true);

        // when
        Boolean doesExist = usersService.checkIfUserExistsById(tPUserStub.getId());

        // then
        assertThat(doesExist).isTrue();
    }

    // checkIfUserExistsByName tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_grantAdminPermissionsIsCalledByAdmin_then_returnCorrespondingUserRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Mockito.when(tPUserRepository.existsByName(tPUserStub.getName())).thenReturn(true);

        // when
        Boolean doesExist = usersService.checkIfUserExistsByName(tPUserStub.getName());

        // then
        assertThat(doesExist).isTrue();
    }
}