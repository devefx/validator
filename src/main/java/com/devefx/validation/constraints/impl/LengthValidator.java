package com.devefx.validation.constraints.impl;

import com.devefx.validation.Script;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.constraints.FieldValidator;
import com.devefx.validation.script.JavaScript;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;

/**
 * LengthValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("LengthValidator.js")
public class LengthValidator extends FieldValidator implements Script {

    private final Script script;
    private int minLen;    // 最小长度
    private int maxLen;    // 最大长度

    public LengthValidator(String field, int minLen, int maxLen, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.minLen = minLen;
        this.maxLen = maxLen;
        script = JavaScript.create(this, field, minLen, maxLen, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (value != null) {
            int len = value.length();
            return len >= minLen && len <= maxLen;
        }
        return false;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
