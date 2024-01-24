package org.trickyplay.trickyplayapi.users.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EditAccountRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void usernameMustHaveLengthLessThanOrEqual16() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("12345678901234567") // 17 letters
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("1234567890123456") // 16 letters
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameMustHaveMinimumLengthOf2() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("1")
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("12")
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void usernameCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("!@#$%^&*()")
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("aA_1")
                .newPassword("asdf1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveLengthLessThanOrEqual32() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("1234567890" + "1234567890" + "1234567890" + "123") // 33 letters
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("1234567890" + "1234567890" + "1234567890" + "12") // 32 letters
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustHaveMinimumLengthOf4() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("123")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("1234")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordMustContainAtLeastOneDigit() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("asdfghjkl")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("1asdfghjkl")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordCanOnlyConsistOfUnderscoresNumbersLowercaseAndUppercaseLetters() {
        EditAccountRequest editAccountBadRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("!@#$%^&*()")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> notEmptyViolations = validator.validate(editAccountBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditAccountRequest editAccountGoodRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword("__12aAbBcC")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> emptyViolations = validator.validate(editAccountGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void passwordAndUsernameCanBeNull() {
        EditAccountRequest editAccountUsernameNullRequest = EditAccountRequest.builder()
                .newUsername(null)
                .newPassword("1234asdf")
                .build();
        Set<ConstraintViolation<EditAccountRequest>> usernameNullEmptyViolations = validator.validate(editAccountUsernameNullRequest);
        assertThat(usernameNullEmptyViolations).isEmpty();

        EditAccountRequest editAccountPasswordNullRequest = EditAccountRequest.builder()
                .newUsername("test")
                .newPassword(null)
                .build();
        Set<ConstraintViolation<EditAccountRequest>> passwordNullEmptyViolations = validator.validate(editAccountPasswordNullRequest);
        assertThat(passwordNullEmptyViolations).isEmpty();

        EditAccountRequest editAccountBothUsernameAndPasswordNullRequest = EditAccountRequest.builder()
                .newUsername(null)
                .newPassword(null)
                .build();
        Set<ConstraintViolation<EditAccountRequest>> editAccountBothUsernameAndPasswordNullEmptyViolations = validator.validate(editAccountBothUsernameAndPasswordNullRequest);
        assertThat(editAccountBothUsernameAndPasswordNullEmptyViolations).isEmpty();
    }
}