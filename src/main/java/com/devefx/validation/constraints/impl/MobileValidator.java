package com.devefx.validation.constraints.impl;

import com.devefx.validation.Script;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.constraints.FieldValidator;
import com.devefx.validation.kit.StrKit;
import com.devefx.validation.script.JavaScript;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MobileValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("MobileValidator.js")
public class MobileValidator extends FieldValidator implements Script {

    private static final Pattern mobilePattern = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[3678]|18[0-9]|14[57])[0-9]{8}$");

    private final Script script;

    public MobileValidator(String field, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        script = JavaScript.create(this, field, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            Matcher matcher = mobilePattern.matcher(value);
            return matcher.matches();
        }
        return true;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
