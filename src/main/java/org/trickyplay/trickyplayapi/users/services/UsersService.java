package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.GetUsersResponse;
import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {
    private final TPUserRepository userRepository;
    public GetUsersResponse getUsers(int pageNumber, int pageSize, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        Page<TPUser> userPage = userRepository.findAll(pageable);
        List<TPUserPublicInfoDTO> users = UserUtils.mapToTPUserPublicInfoDTOs(userPage.getContent());

        return GetUsersResponse.builder()
                .users(users)
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .pageSize(userPage.getSize())
                .pageNumber(userPage.getNumber())
                .isLast(userPage.isLast())
                .build();
    }

    public TPUserPublicInfoDTO getUser(long id) {
        return userRepository.findById(id)
                .map(UserUtils::mapToTPUserPublicInfoDTO)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public boolean checkIfUserExistsById(long id){
        return userRepository.existsById(id);
    }

    public boolean checkIfUserExistsByName(String name){
        return userRepository.existsByName(name);
    }
}
