package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "comment", collectionRelation = "comments")
public class CommentRepresentation extends RepresentationModel<CommentRepresentation> {
    private long id;
    private String body;
    private String gameName;
    private TPUserRepresentation author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
