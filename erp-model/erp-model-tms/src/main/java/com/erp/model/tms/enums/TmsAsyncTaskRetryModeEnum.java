package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * TMS 异步任务重试模式枚举
 * full：全量重试，创建新任务按当前数据库状态重新查询
 * failed_only：错误明细重试，复用当前任务重跑失败或超时明细
 * 非重试任务存空字符串，不存此枚举值
 */
public enum TmsAsyncTaskRetryModeEnum implements EnumMessage {

    FULL("full", "全量重试"),
    FAILED_ONLY("failed_only", "错误明细重试"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    TmsAsyncTaskRetryModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TmsAsyncTaskRetryModeEnum e : values()) {
            if (e.code.equals(code)) {
                return e.name;
            }
        }
        return "";
    }

    public static TmsAsyncTaskRetryModeEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
