package org.trickyplay.trickyplayapi.replies.dtos;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditReplyRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String newReplyBody;
}