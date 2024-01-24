package org.trickyplay.trickyplayapi.users.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void refreshTokenCantBeBlank() {
        RefreshTokenRequest refreshTokenBadRequest = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();
        Set<ConstraintViolation<RefreshTokenRequest>> notEmptyViolations = validator.validate(refreshTokenBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        RefreshTokenRequest refreshTokenGoodRequest = RefreshTokenRequest.builder()
                .refreshToken("asdf")
                .build();
        Set<ConstraintViolation<RefreshTokenRequest>> emptyViolations = validator.validate(refreshTokenGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }
}