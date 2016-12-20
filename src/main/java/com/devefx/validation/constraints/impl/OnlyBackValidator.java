package com.devefx.validation.constraints.impl;

import javax.servlet.http.HttpServletRequest;

import com.devefx.validation.ConstraintValidator;

public class OnlyBackValidator extends ConstraintValidator {

    private ConstraintValidator validator;
    
    public OnlyBackValidator(ConstraintValidator validator) {
        super(validator.getError().getCode(), validator.getError().getMessage());
        this.validator = validator;
    }

    @Override
    public boolean isValid(HttpServletRequest request) throws Exception {
        return validator.isValid(request);
    }

}
