package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SignInResponse extends RepresentationModel {
    //        @JsonProperty("access_token")
    private String accessToken;

    //        @JsonProperty("refresh_token")
    private String refreshToken;

    private TPUserRepresentation userPublicInfo;
}
