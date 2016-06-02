package com.devefx.validation.constraints.impl;

import com.devefx.validation.constraints.FieldValidator;
import com.devefx.validation.kit.StrKit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * SessionValidator
 * Created by YYQ on 2016/5/27.
 */
public class SessionValidator extends FieldValidator {

    private String sessionKey;

    public SessionValidator(String field, String sessionKey, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.sessionKey = sessionKey;
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            HttpSession session = request.getSession();
            return value.equals(session.getAttribute(sessionKey));
        }
        return true;
    }
}
