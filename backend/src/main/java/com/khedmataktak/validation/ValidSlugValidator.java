package com.khedmataktak.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidSlugValidator implements ConstraintValidator<ValidSlug, String> {

    private final SlugValidator slugValidator;

    public ValidSlugValidator(SlugValidator slugValidator) {
        this.slugValidator = slugValidator;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return slugValidator.isValidFormat(value) && !slugValidator.isReserved(value);
    }
}
