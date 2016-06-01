package com.devefx.validation;

/**
 * Result
 * Created by YYQ on 2016/6/1.
 */
public class Result<T> {

    private boolean success;
    private T data;
    private String message;

    public Result(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
