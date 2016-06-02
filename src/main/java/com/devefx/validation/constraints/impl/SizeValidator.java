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
 * SizeValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("SizeValidator.js")
public class SizeValidator extends FieldValidator implements Script {

    private final Script script;
    private long min;
    private long max;

    public SizeValidator(String field, long min, long max, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.min = min;
        this.max = max;
        script = JavaScript.create(this, field, min, max, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            try {
                Long val = Long.parseLong(value);
                return val >= min && val <= max;
            } catch (NumberFormatException e) {
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
