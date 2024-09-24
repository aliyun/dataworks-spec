package com.aliyun.migrationx.common.exception;

public class UnSupportedTypeException extends RuntimeException {
    private String type;

    public UnSupportedTypeException(String type) {
        super("unsupported converter task type:  " + type);
        this.type = type;
    }
}
