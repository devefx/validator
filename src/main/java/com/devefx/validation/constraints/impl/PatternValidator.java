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
 * PatternValidator
 * Created by YYQ on 2016/5/26.
 */
@BindScript("PatternValidator.js")
public class PatternValidator extends FieldValidator implements Script {

    private final Script script;
    private final Pattern pattern;

    public PatternValidator(String field, String regExpression, boolean isCaseSensitive, String errorCode, String errorMessage) {
        super(field, errorCode, errorMessage);
        this.pattern = isCaseSensitive ? Pattern.compile(regExpression) :
                Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE);
        script = JavaScript.create(this, field, regExpression, isCaseSensitive, errorCode, errorMessage);
    }

    public PatternValidator(String field, String regExpression, String errorCode, String errorMessage) {
        this(field, regExpression, false, errorCode, errorMessage);
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        String value = request.getParameter(field);
        if (!StrKit.isEmpty(value)) {
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        }
        return true;
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
