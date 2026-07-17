package com.common.business.mask.protect;

import com.common.core.exception.ServiceException;

/**
 * 脱敏回显保护异常。
 *
 * @author cloud-erp
 */
public class MaskProtectException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_MESSAGE = "页面数据已过期或敏感字段状态已变化，请刷新后重试";

    public MaskProtectException() {
        super(DEFAULT_MESSAGE);
    }

    public MaskProtectException(String message) {
        super(message);
    }
}
