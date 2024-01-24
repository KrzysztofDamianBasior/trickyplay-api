package org.trickyplay.trickyplayapi.users.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignUpRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void usernameMustHaveLengthLessThanOrEqual16() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("12345678901234567") // 17 letters
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("1234567890123456") // 16 letters
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameMustHaveMinimumLengthOf2() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("1")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("12")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("!@#$%^&*()")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("aA_1")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveLengthLessThanOrEqual32() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("test")
                .password("1234567890" + "1234567890" + "1234567890" + "123") // 33 letters
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("1234567890" + "1234567890" + "1234567890" + "12") // 32 letters
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveMinimumLengthOf4() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("test")
                .password("123")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("1234")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustContainAtLeastOneDigit() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("test")
                .password("asdfghjkl")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("1asdfghjkl")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("test")
                .password("!@#$%^&*()")
                .build();
        Set<ConstraintViolation<SignUpRequest>> notEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("__12aAbBcC")
                .build();
        Set<ConstraintViolation<SignUpRequest>> emptyViolations = validator.validate(signUpGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordCantBeNull() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username("test")
                .password(null)
                .build();
        Set<ConstraintViolation<SignUpRequest>> signUpNotEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(signUpNotEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignUpRequest>> signUpEmptyViolations = validator.validate(signUpGoodRequest);
        assertThat(signUpEmptyViolations).isEmpty();
    }

    @Test
    void usernameCantBeNull() {
        SignUpRequest signUpBadRequest = SignUpRequest.builder()
                .username(null)
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignUpRequest>> signUpNotEmptyViolations = validator.validate(signUpBadRequest);
        assertThat(signUpNotEmptyViolations).isNotEmpty();

        SignUpRequest signUpGoodRequest = SignUpRequest.builder()
                .username("test")
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignUpRequest>> signUpEmptyViolations = validator.validate(signUpGoodRequest);
        assertThat(signUpEmptyViolations).isEmpty();
    }
}