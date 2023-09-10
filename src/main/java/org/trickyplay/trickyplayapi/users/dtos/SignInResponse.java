package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
public class SignInResponse {
    //        @JsonProperty("access_token")
    private String accessToken;

    //        @JsonProperty("refresh_token")
    private String refreshToken;
}
