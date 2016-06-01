package com.devefx.validation;

import java.io.IOException;
import java.io.Writer;

/**
 * Script
 * Created by YYQ on 2016/5/30.
 */
public interface Script {
    void output(Writer out) throws IOException;
}
