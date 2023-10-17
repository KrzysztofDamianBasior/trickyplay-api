package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditCommentRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String newCommentBody;
}
