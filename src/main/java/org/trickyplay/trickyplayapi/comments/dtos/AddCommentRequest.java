package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AddCommentRequest extends RepresentationModel {
    @NotNull
    @Length(min = 1, max = 300)
    private String body;

    @NotNull
    @Pattern(regexp = "^(Snake|TicTacToe|Minesweeper)$", message = "invalid game name")
    private String gameName;
}
