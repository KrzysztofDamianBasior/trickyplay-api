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
public class EditReplyRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String newReplyBody;

    @NotNull
    @Min(0)
    private Long replyId;
}