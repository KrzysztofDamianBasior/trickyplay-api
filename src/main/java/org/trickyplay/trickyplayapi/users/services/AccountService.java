package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.DeleteAccountResponse;
import org.trickyplay.trickyplayapi.users.dtos.EditAccountRequest;
import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final TPUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TPUserPublicInfoDTO grantAdminPermissions(long idOfTheUserToWhomPermissionsAreGranted) {
        TPUser user = userRepository.findById(idOfTheUserToWhomPermissionsAreGranted)
                .orElseThrow(() -> new UserNotFoundException(idOfTheUserToWhomPermissionsAreGranted));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You cannot modify the permissions of the admin account");
        }
        user.setRole(Role.ADMIN);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public TPUserPublicInfoDTO getAccount(long id) {
        return userRepository.findById(id)
                .map(UserUtils::mapToTPUserPublicInfoDTO)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public DeleteAccountResponse deleteAccount(long id) {
        userRepository.deleteById(id);
        return DeleteAccountResponse.builder()
                .message("The account for user with id: " + id + " has been removed")
                .build();
    }

    public TPUserPublicInfoDTO editAccount(long accountOwnerId, EditAccountRequest editAccountRequest) {
        TPUser user = userRepository.findById(accountOwnerId)
                .orElseThrow(() -> new UserNotFoundException(accountOwnerId));
        if (editAccountRequest.getNewUsername() != null) {
            user.setName(editAccountRequest.getNewUsername());
            user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        if (editAccountRequest.getNewPassword() != null) {
            user.setPassword(passwordEncoder.encode(editAccountRequest.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public TPUserPublicInfoDTO banAccount(long id) {
        TPUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You can't ban an admin");
        }
        user.setRole(Role.BANNED);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }

    public TPUserPublicInfoDTO unbanAccount(long id) {
        TPUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if(user.getRole().equals(Role.ADMIN)){
            throw new OperationNotAllowedException("You cannot modify the permissions of the admin account");
        }
        user.setRole(Role.USER);
        TPUser savedUser = userRepository.save(user);
        return UserUtils.mapToTPUserPublicInfoDTO(savedUser);
    }
}
