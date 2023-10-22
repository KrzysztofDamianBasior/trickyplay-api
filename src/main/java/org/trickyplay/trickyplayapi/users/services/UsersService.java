package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.dtos.GetUsersResponse;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.records.UsersPageArgs;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {
    private final TPUserRepository userRepository;

    public GetUsersResponse getUsers(UsersPageArgs usersPageArgs) {
        Pageable pageable = PageRequest.of(
                usersPageArgs.pageNumber(),
                usersPageArgs.pageSize(),
                usersPageArgs.orderDirection(),
                usersPageArgs.sortBy()
        );
        Page<TPUser> userPage = userRepository.findAll(pageable);
        List<TPUserRepresentation> users = UserUtils.mapToTPUserPublicInfoDTOs(userPage.getContent());

        GetUsersResponse getUsersResponse = GetUsersResponse.builder()
                .users(users)
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .pageSize(userPage.getSize())
                .pageNumber(userPage.getNumber())
                .isLast(userPage.isLast())
                .build();
        getUsersResponse.add(linkTo(methodOn(UsersController.class)
                .getUsers(
                        usersPageArgs.pageNumber(),
                        usersPageArgs.pageSize(),
                        usersPageArgs.sortBy(),
                        usersPageArgs.orderDirection().name()
                )).withSelfRel());

        return getUsersResponse;
    }

    public TPUserRepresentation getUser(long id) {
        return userRepository.findById(id)
                .map(UserUtils::mapToTPUserPublicInfoDTO)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public TPUserRepresentation grantAdminPermissions(long idOfTheUserToWhomPermissionsAreGranted) {
        TPUser user = userRepository.findById(idOfTheUserToWhomPermissionsAreGranted)
                .orElseThrow(() -> new UserNotFoundException(idOfTheUserToWhomPermissionsAreGranted));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You cannot modify the permissions of the admin account");
        }
        user.setRole(Role.ADMIN);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public TPUserRepresentation banUser(long id) {
        TPUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You can't ban an admin");
        }
        user.setRole(Role.BANNED);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public TPUserRepresentation unbanUser(long id) {
        TPUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You cannot modify the permissions of the admin account");
        }
        user.setRole(Role.USER);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public boolean checkIfUserExistsById(long id) {
        return userRepository.existsById(id);
    }

    public boolean checkIfUserExistsByName(String name) {
        return userRepository.existsByName(name);
    }
}
