package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
public class SignInRequest {
    @NotNull
    @Length(min = 2, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,16}$")
    private String username;

    @NotNull
    @Length(min = 4, max = 32)
    @Pattern(regexp = "^(?=.*[0-9])[a-zA-Z0-9_]{4,32}$")
    private String password;
}
