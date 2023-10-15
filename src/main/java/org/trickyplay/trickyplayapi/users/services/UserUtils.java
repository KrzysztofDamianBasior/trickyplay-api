package org.trickyplay.trickyplayapi.users.services;

import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {
    private UserUtils(){}

    public static List<TPUserPublicInfoDTO> mapToTPUserPublicInfoDTOs(List<TPUser> users) {
        return users.stream()
                .map(UserUtils::mapToTPUserPublicInfoDTO)
                .collect(Collectors.toList());
    }

    public static TPUserPublicInfoDTO mapToTPUserPublicInfoDTO(TPUser user) {
        return TPUserPublicInfoDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
