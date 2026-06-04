package com.common.core.exception;

/**
 * 三方仓接口空响应异常。
 */
public class ThirdWarehouseEmptyResponseException extends ServiceException {

    public ThirdWarehouseEmptyResponseException(String msg, Object... args) {
        super(msg, args);
    }
}
