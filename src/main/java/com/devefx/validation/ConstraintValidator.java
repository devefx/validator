package com.devefx.validation;

import com.devefx.validation.Error;

import javax.servlet.http.HttpServletRequest;

/**
 * ConstraintValidator
 * Created by YYQ on 2016/5/26.
 */
public abstract class ConstraintValidator {

    protected final Error error;

    public ConstraintValidator(String errorCode, String errorMessage) {
        error = new Error(errorCode, errorMessage);
    }

    public Error getError() {
        return error;
    }

    public abstract boolean isValid(HttpServletRequest request) throws Exception;
}
