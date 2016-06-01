package com.devefx.validation.constraints.impl;

import com.devefx.validation.Script;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.constraints.FieldValidator;
import com.devefx.validation.script.JavaScript;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;

/**
 * NotNullValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("NotNullValidator.js")
public class NotNullValidator extends FieldValidator implements Script {

    private final Script script;

    public NotNullValidator(String field, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        script = JavaScript.create(this, field, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        return request.getParameter(field) != null;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
