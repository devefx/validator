package com.devefx.validation;

/**
 * Error
 * Created by YYQ on 2016/5/26.
 */
public class Error {
    private String code;
    private String message;

    public Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return code.equals(error.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
