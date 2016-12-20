package com.devefx.validation.script;

import com.devefx.validation.ConstraintValidator;
import com.devefx.validation.Script;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * JavaScript
 * Created by YYQ on 2016/5/30.
 */
public class JavaScript implements Script {

    protected StringBuffer buffer;

    public JavaScript(ConstraintValidator validator, Object... args) {
        this(validator.getClass().getSimpleName(), args);
    }

    public JavaScript(String functionName, Object... args) {
        buffer = new StringBuffer(functionName);
        buffer.append("(");
        for (int i = 0; i < args.length; i++) {
            buffer.append(i != 0 ? ", " : "");
            if (args[i] instanceof String) {
                buffer.append("\"" + args[i] + "\"");
            } else if (args[i] instanceof Date) {
                throw new IllegalArgumentException();
            } else {
                buffer.append(args[i]);
            }
        }
        buffer.append(")");
    }
    
    @Override
    public void output(Writer out) throws IOException {
        out.write(buffer.toString());
    }

    public static JavaScript create(ConstraintValidator validator, Object... args) {
        return new JavaScript(validator, args);
    }
    
    public static JavaScript create(String functionName, Object... args) {
        return new JavaScript(functionName, args);
    }
}
