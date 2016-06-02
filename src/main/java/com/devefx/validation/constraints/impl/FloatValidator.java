package com.devefx.validation.constraints.impl;

import com.devefx.validation.Script;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.constraints.FieldValidator;
import com.devefx.validation.kit.StrKit;
import com.devefx.validation.script.JavaScript;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;

/**
 * FloatValidator
 * Created by YYQ on 2016/6/1.
 */
@BindScript("FloatValidator.js")
public class FloatValidator extends FieldValidator implements Script {

    private final Script script;

    public FloatValidator(String field, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.script = JavaScript.create(this, field, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) throws Exception {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            try {
                Float.parseFloat(value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
