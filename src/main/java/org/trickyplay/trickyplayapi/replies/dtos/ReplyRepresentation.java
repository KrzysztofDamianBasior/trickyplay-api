package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "comment", collectionRelation = "comments")
public class ReplyRepresentation extends RepresentationModel<ReplyRepresentation> {
    private Long id;
    private String body;
    private TPUserPublicInfoDTO author;
    private LocalDateTime createdAt; //ISO-8601 UTC
    private LocalDateTime updatedAt; //ISO-8601 UTC
}
