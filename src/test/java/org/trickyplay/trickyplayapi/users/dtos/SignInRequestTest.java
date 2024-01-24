package org.trickyplay.trickyplayapi.users.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignInRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void usernameMustHaveLengthLessThanOrEqual16() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("12345678901234567") // 17 letters
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("1234567890123456") // 16 letters
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameMustHaveMinimumLengthOf2() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("1")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("12")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("!@#$%^&*()")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("aA_1")
                .password("asdf1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveLengthLessThanOrEqual32() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("test")
                .password("1234567890" + "1234567890" + "1234567890" + "123") // 33 letters
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("1234567890" + "1234567890" + "1234567890" + "12") // 32 letters
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveMinimumLengthOf4() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("test")
                .password("123")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("1234")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustContainAtLeastOneDigit() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("test")
                .password("asdfghjkl")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("1asdfghjkl")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("test")
                .password("!@#$%^&*()")
                .build();
        Set<ConstraintViolation<SignInRequest>> notEmptyViolations = validator.validate(signInBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("__12aAbBcC")
                .build();
        Set<ConstraintViolation<SignInRequest>> emptyViolations = validator.validate(signInGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordCantBeNull() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username("test")
                .password(null)
                .build();
        Set<ConstraintViolation<SignInRequest>> signInNotEmptyViolations = validator.validate(signInBadRequest);
        assertThat(signInNotEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignInRequest>> signInEmptyViolations = validator.validate(signInGoodRequest);
        assertThat(signInEmptyViolations).isEmpty();
    }

    @Test
    void usernameCantBeNull() {
        SignInRequest signInBadRequest = SignInRequest.builder()
                .username(null)
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignInRequest>> signInNotEmptyViolations = validator.validate(signInBadRequest);
        assertThat(signInNotEmptyViolations).isNotEmpty();

        SignInRequest signInGoodRequest = SignInRequest.builder()
                .username("test")
                .password("1234asdf")
                .build();
        Set<ConstraintViolation<SignInRequest>> signInEmptyViolations = validator.validate(signInGoodRequest);
        assertThat(signInEmptyViolations).isEmpty();
    }
}