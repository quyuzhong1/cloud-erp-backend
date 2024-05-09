package com.erp.server.file.exception;

public class TaskNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8766616531224883007L;

    private static final String MESSAGE = "任务不存在";

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}