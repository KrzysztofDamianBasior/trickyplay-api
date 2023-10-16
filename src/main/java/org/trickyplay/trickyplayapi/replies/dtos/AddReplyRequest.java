package org.trickyplay.trickyplayapi.replies.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
public class AddReplyRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String body;

    @NotNull
    @Min(0)
    private Long parentCommentId;
}
