package org.trickyplay.trickyplayapi.users.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SignOutResponse extends RepresentationModel {
    private int numberOfRefreshTokensRemoved;
    private String message;
}
