package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    @NotNull
    @Length(min = 1, max = 300)
    private String body;

    @NotNull
    @Pattern(regexp = "^(Snake|TicTacToe|Minesweeper)$", message = "invalid game name")
    private String gameName;
}
