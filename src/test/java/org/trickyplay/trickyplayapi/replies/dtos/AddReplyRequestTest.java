package org.trickyplay.trickyplayapi.replies.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

class AddReplyRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bodyMustNotBeNull() {
        AddReplyRequest addReplyRequest = AddReplyRequest.builder()
                .body(null)
                .parentCommentId(5L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> violations = validator.validate(addReplyRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void parentCommentIdMustNotBeNull() {
        AddReplyRequest addReplyRequest = AddReplyRequest.builder()
                .body("test")
                .parentCommentId(null)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> violations = validator.validate(addReplyRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void minimumParentCommentIdValueIs0() {
        AddReplyRequest addReplyBadRequest = AddReplyRequest.builder()
                .body("test")
                .parentCommentId(-1L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> notEmptyViolations = validator.validate(addReplyBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddReplyRequest addReplyGoodRequest = AddReplyRequest.builder()
                .body("test")
                .parentCommentId(0L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> emptyViolations = validator.validate(addReplyGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void bodyMustHaveLengthLessThanOrEqual300() {
        AddReplyRequest addReplyBadRequest = AddReplyRequest.builder()
                .body( // 301 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1")
                .parentCommentId(5L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> notEmptyViolations = validator.validate(addReplyBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddReplyRequest addReplyGoodRequest = AddReplyRequest.builder()
                .body(// 300 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                )
                .parentCommentId(5L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> emptyViolations = validator.validate(addReplyGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void bodyMustHaveMinimumLengthOf1() {
        AddReplyRequest addReplyBadRequest = AddReplyRequest.builder()
                .body("")
                .parentCommentId(5L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> notEmptyViolations = validator.validate(addReplyBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddReplyRequest addReplyGoodRequest = AddReplyRequest.builder()
                .body("a")
                .parentCommentId(5L)
                .build();
        Set<ConstraintViolation<AddReplyRequest>> emptyViolations = validator.validate(addReplyGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }
}