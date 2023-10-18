package org.trickyplay.trickyplayapi.users.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "user", collectionRelation = "users")
public class TPUserRepresentation extends RepresentationModel {
    private Long id;

    private String name;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
