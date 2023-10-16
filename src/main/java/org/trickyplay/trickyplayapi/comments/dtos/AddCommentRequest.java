package org.trickyplay.trickyplayapi.comments.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
