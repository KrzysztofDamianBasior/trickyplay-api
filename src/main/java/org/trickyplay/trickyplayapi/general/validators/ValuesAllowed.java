package org.trickyplay.trickyplayapi.general.validators;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;


// ref: https://stackoverflow.com/questions/59422883/spring-boot-custom-validation-in-request-params
// ref: https://reflectoring.io/bean-validation-with-spring-boot/
// usage: @ValuesAllowed(propName = "orderBy", values = { "OpportunityCount", "OpportunityPublishedCount", "ApplicationCount", "ApplicationsApprovedCount" }) @RequestParam(required = false) String orderBy

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValuesAllowed.Validator.class })
public @interface ValuesAllowed {

    String message() default "Field value should be from list of "; // the parameter message, pointing to a property key in ValidationMessages.properties, which is used to resolve a message in case of violation,

    Class<?>[] groups() default {}; // the parameter groups, allowing to define under which circumstances this validation is to be triggered

    Class<? extends Payload>[] payload() default {}; // the parameter payload, allowing to define a payload to be passed with this validation

    String propName();

    String[] values();

    class Validator implements ConstraintValidator<ValuesAllowed, String> {
        private String propName;
        private String message;
        private List<String> allowable;

        @Override
        public void initialize(ValuesAllowed requiredIfChecked) {
            this.propName = requiredIfChecked.propName();
            this.message = requiredIfChecked.message();
            this.allowable = Arrays.asList(requiredIfChecked.values());
        }

        public boolean isValid(String value, ConstraintValidatorContext context) {
            Boolean valid = value == null || this.allowable.contains(value);

            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message.concat(this.allowable.toString()))
                        .addPropertyNode(this.propName).addConstraintViolation();
            }
            return valid;
        }
    }
}