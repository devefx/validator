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
 * EmailValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("EmailValidator.js")
public class EmailValidator extends FieldValidator implements Script {

    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern emailMultiPattern = Pattern.compile("^([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+;)+$", Pattern.CASE_INSENSITIVE);

    private final Script script;
    private final boolean multi;

    public EmailValidator(String field, boolean multi, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.multi = multi;
        script = JavaScript.create(this, field, multi, errorCode, errorMessage);
    }

    public EmailValidator(String field, String errorCode, String errorMessage) {
        this(field, false, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            if (multi) {
                if (!value.endsWith(";")) {
                    value = value + ";";
                }
                Matcher matcher = emailMultiPattern.matcher(value);
                return matcher.matches();
            }
            Matcher matcher = emailPattern.matcher(value);
            return matcher.matches();
        }
        return true;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
