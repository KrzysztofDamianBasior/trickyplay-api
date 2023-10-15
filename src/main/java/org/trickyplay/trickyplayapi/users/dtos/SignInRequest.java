package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {
    @NotNull
    @Length(min = 2, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,16}$", message = "Username must contain between 2 and 16 characters. It can only consist of underscores, numbers, lowercase and uppercase letters.")
    private String username;

    @NotNull
    @Length(min = 4, max = 32)
    @Pattern(regexp = "^(?=.*[0-9])[a-zA-Z0-9_]{4,32}$", message = "Password must contain between 4 and 32 characters. It must contain at least one number. The password can only consist of underscores, numbers, lowercase and uppercase letters.")
    private String password;
}
