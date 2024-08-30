package com.erp.server.file.exception;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 8766616531224883007L;

    public BusinessException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}