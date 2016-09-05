package com.devefx.validation.constraints;

import com.devefx.validation.ConstraintValidator;

/**
 * FieldValidator
 * Created by YYQ on 2016/5/26.
 */
public abstract class FieldValidator extends ConstraintValidator {

    protected final String field;

    public FieldValidator(String field, String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.field = field;
    }
}
