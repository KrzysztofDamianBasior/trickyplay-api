package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeleteCommentResponse extends RepresentationModel {
    private String message;
}
