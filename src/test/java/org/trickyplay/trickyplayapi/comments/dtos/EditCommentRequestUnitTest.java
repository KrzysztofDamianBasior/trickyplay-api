package org.trickyplay.trickyplayapi.comments.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

class EditCommentRequestUnitTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bodyMustNotBeNull() {
        EditCommentRequest editCommentRequest = EditCommentRequest.builder()
                .newCommentBody(null)
                .build();
        Set<ConstraintViolation<EditCommentRequest>> violations = validator.validate(editCommentRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bodyMustHaveLengthLessThanOrEqual300() {
        EditCommentRequest editCommentRequest = EditCommentRequest.builder()
                .newCommentBody(    // 301 words
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1"
                )
                .build();
        Set<ConstraintViolation<EditCommentRequest>> violations = validator.validate(editCommentRequest);
        assertThat(violations).isNotEmpty();

        EditCommentRequest editGoodCommentRequest = EditCommentRequest.builder()
                .newCommentBody(    // 300 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                )
                .build();
        Set<ConstraintViolation<EditCommentRequest>> emptyViolations = validator.validate(editGoodCommentRequest);
        assertThat(emptyViolations).isEmpty();
    }


    @Test
    void bodyMustHaveMinimumLengthOf1() {
        EditCommentRequest editCommentBadRequest = EditCommentRequest.builder()
                .newCommentBody("")
                .build();
        Set<ConstraintViolation<EditCommentRequest>> notEmptyViolations = validator.validate(editCommentBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditCommentRequest editCommentGoodRequest = EditCommentRequest.builder()
                .newCommentBody("a")
                .build();
        Set<ConstraintViolation<EditCommentRequest>> emptyViolations = validator.validate(editCommentGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }
}