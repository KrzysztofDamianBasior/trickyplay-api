package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EditCommentRequest extends RepresentationModel {
    @NotNull
    @Length(min = 1, max = 300)
    private String newCommentBody;
}
