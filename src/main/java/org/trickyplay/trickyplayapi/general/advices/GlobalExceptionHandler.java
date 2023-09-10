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
    public String handleReplyNotFoundException(ReplyNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CommentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCommentNotFoundException(CommentNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // ref: https://stackoverflow.com/questions/37781632/fielderror-vs-objecterror-vs-global-error
        // getAllErrors() returns all errors, both Global and Field. getFieldErrors() only returns errors related to binding field values
        // difference between FieldError, ObjectError and global error, all in the context of a BindingResult
        // "global error" is any ObjectError that is not an instance of a FieldError, getGlobalError() returns an ObjectError

        // if you only log FieldErrors, you will miss any ObjectErrors that code registered as a "global error" e.g. by calling BindingResult.reject(errorCode, errorArgs, defaultMessage).
        // Typically, errors are registered against fields of the validated/bound object as opposed to the object itself.

        // Following are a couple of ways to create global errors:
        //      Assuming it's the root form object and not a nested object that's being validated via a Spring Validator implementation, you could add a "global error" (in the context of the specified bound root object) by passing null as the field name parameter of rejectValue. If the object being validated is a nested object, however, a FieldError would be registered against the nested object field. So it matters what is the nestedPath ("nested object graph") property of the target Errors object with respect to whether a general ObjectError or specific FieldError is added.
        //      Via a JSR 303 constraint annotation applied at the class level. See example in which a model object is checked for pre-existence in a datastore.

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
