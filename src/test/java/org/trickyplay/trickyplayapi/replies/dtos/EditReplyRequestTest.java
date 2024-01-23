package org.trickyplay.trickyplayapi.replies.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EditReplyRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bodyMustNotBeNull() {
        EditReplyRequest editReplyRequest = EditReplyRequest.builder()
                .newReplyBody(null)
                .build();
        Set<ConstraintViolation<EditReplyRequest>> violations = validator.validate(editReplyRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bodyMustHaveLengthLessThanOrEqual300() {
        EditReplyRequest editReplyBadRequest = EditReplyRequest.builder()
                .newReplyBody( // 301 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1")
                .build();
        Set<ConstraintViolation<EditReplyRequest>> notEmptyViolations = validator.validate(editReplyBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditReplyRequest editReplyGoodRequest = EditReplyRequest.builder()
                .newReplyBody(// 300 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                )
                .build();
        Set<ConstraintViolation<EditReplyRequest>> emptyViolations = validator.validate(editReplyGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void bodyMustHaveMinimumLengthOf1() {
        EditReplyRequest editReplyBadRequest = EditReplyRequest.builder()
                .newReplyBody("")
                .build();
        Set<ConstraintViolation<EditReplyRequest>> notEmptyViolations = validator.validate(editReplyBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        EditReplyRequest editReplyGoodRequest = EditReplyRequest.builder()
                .newReplyBody("a")
                .build();
        Set<ConstraintViolation<EditReplyRequest>> emptyViolations = validator.validate(editReplyGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }
}