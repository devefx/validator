package com.devefx.validation.constraints;

import java.io.IOException;
import java.io.Writer;

import com.devefx.validation.Script;
import com.devefx.validation.script.JavaScript;

public abstract class AsyncFieldValidator extends FieldValidator implements Script {

    static final String ASYNC_VALIDATOR = "AsyncValidator";
    
    private final Script script;
    
    public AsyncFieldValidator(String field, String errorCode,
            String errorMessage) {
        super(field, errorCode, errorMessage);
        this.script = JavaScript.create(ASYNC_VALIDATOR, field, errorCode, errorMessage);
    }

    @Override
    public void output(Writer out) throws IOException {
        script.output(out);
    }
}
