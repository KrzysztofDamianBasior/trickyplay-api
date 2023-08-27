package org.trickyplay.trickyplayapi.general.advices;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.trickyplay.trickyplayapi.general.exceptions.CommentNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.ReplyNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ReplyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String replyNotFoundHandler(ReplyNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CommentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String commentNotFoundHandler(CommentNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String userNotFoundHandler(UserNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {}
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Map<String, String> results = new HashMap<>();
        // ex.getBindingResult().getAllErrors()
        //        .stream()
        //        .filter(FieldError.class::isInstance)
        //        .map(FieldError.class::cast)
        //        .forEach(fieldError -> results.put(fieldError.getField(), fieldError.getDefaultMessage()));

        log.debug(ex.getMessage());

        return errors;
        // when we use @ControllerAdvice instead of @RestControllerAdvice we need to return ResponseEntity type
        // and determine the response type in return like:
        // return ResponseEntity.badRequest().body(errors);
    }
}
