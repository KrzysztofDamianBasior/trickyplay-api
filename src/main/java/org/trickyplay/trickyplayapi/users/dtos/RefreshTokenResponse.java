package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RefreshTokenResponse {
    private final String accessToken;
}
