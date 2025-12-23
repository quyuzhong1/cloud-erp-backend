package com.erp.sdk.oms.yunting.cem.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 云听CEM API错误码枚举
 *
 * @author ERP System
 */
@Getter
@AllArgsConstructor
public enum YuntingErrorCodeEnum {

    /**
     * 成功
     */
    SUCCESS(20000, "操作成功"),

    /**
     * 参数错误 - projectId无效
     */
    INVALID_PROJECT_ID(1001, "参数projectId无效"),

    /**
     * 参数错误 - startTime或endTime无效
     */
    INVALID_TIME_PARAM(1002, "参数startTime或endTime无效"),

    /**
     * 参数错误 - pageToken无效
     */
    INVALID_PAGE_TOKEN(1003, "参数pageToken无效"),

    /**
     * 认证失败
     */
    AUTH_FAILED(2001, "认证失败，access_token已过期"),

    /**
     * 项目权限未配置
     */
    PROJECT_PERMISSION_NOT_CONFIGURED(2002, "项目权限未配置"),

    /**
     * 频率限制
     */
    RATE_LIMIT_EXCEEDED(3001, "频率限制，短时间内调用次数已达到限制"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(40002, "操作失败"),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR(-1, "未知错误");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误描述
     */
    private final String description;

    /**
     * 根据错误码获取枚举
     */
    public static YuntingErrorCodeEnum getByCode(Integer code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        for (YuntingErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }

    /**
     * 判断是否成功
     */
    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }
}

