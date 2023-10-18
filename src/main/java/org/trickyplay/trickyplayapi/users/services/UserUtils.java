package org.trickyplay.trickyplayapi.users.services;

import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class UserUtils {
    private UserUtils() {
    }

    public static List<TPUserRepresentation> mapToTPUserPublicInfoDTOs(List<TPUser> users) {
        return users.stream()
                .map(UserUtils::mapToTPUserPublicInfoDTO)
                .collect(Collectors.toList());
    }

    public static TPUserRepresentation mapToTPUserPublicInfoDTO(TPUser user) {
        TPUserRepresentation tpUserRepresentation = TPUserRepresentation.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        tpUserRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUser(tpUserRepresentation.getId()))
                .withSelfRel());
        tpUserRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUsers(0, 10, "id", "Asc"))
                .withRel("collection"));
        tpUserRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUserComments(tpUserRepresentation.getId(), 0, 10, "id", "Asc"))
                .withRel("comments-by"));
        tpUserRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUserReplies(tpUserRepresentation.getId(), 0, 10, "id", "Asc"))
                .withRel("replies-by"));
        return tpUserRepresentation;
    }
}
