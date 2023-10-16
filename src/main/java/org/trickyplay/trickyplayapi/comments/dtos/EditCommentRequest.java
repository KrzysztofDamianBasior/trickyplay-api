package org.trickyplay.trickyplayapi.comments.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditCommentRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String newCommentBody;

    @NotNull
    @Min(0)
    private Long commentId;
}
