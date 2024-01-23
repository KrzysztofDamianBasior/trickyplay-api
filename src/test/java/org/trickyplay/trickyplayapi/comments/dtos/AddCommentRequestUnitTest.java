package org.trickyplay.trickyplayapi.comments.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

class AddCommentRequestUnitTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bodyMustNotBeNull() {
        AddCommentRequest addCommentRequest = AddCommentRequest.builder()
                .body(null)
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> violations = validator.validate(addCommentRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void gameNameMustNotBeNull() {
        AddCommentRequest addCommentRequest = AddCommentRequest.builder()
                .body("test")
                .gameName(null)
                .build();
        Set<ConstraintViolation<AddCommentRequest>> violations = validator.validate(addCommentRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bodyMustHaveLengthLessThanOrEqual300() {
        AddCommentRequest addBadCommentRequest = AddCommentRequest.builder()
                .body(    // 301 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1"
                )
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> notEmptyViolations = validator.validate(addBadCommentRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddCommentRequest addGoodCommentRequest = AddCommentRequest.builder()
                .body(    // 300 letters
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                )
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> emptyViolations = validator.validate(addGoodCommentRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void bodyMustHaveMinimumLengthOf1() {
        AddCommentRequest addCommentBadRequest = AddCommentRequest.builder()
                .body("")
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> notEmptyViolations = validator.validate(addCommentBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddCommentRequest addCommentGoodRequest = AddCommentRequest.builder()
                .body("a")
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> emptyViolations = validator.validate(addCommentGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }

    @Test
    void gameNameHasToBeValid() {
        AddCommentRequest addCommentBadRequest = AddCommentRequest.builder()
                .body("This is just a test comment")
                .gameName("test")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> notEmptyViolations = validator.validate(addCommentBadRequest);
        assertThat(notEmptyViolations).isNotEmpty();

        AddCommentRequest addCommentGoodRequest = AddCommentRequest.builder()
                .body("This is just a test comment")
                .gameName("Snake")
                .build();
        Set<ConstraintViolation<AddCommentRequest>> emptyViolations = validator.validate(addCommentGoodRequest);
        assertThat(emptyViolations).isEmpty();
    }
}