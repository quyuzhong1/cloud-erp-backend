package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * TMS 异步任务执行结果状态枚举
 * 与 status（生命周期）分离，专门描述任务的执行结果。
 * canceled 本阶段仅预留枚举值，不实现触发逻辑。
 */
public enum TmsAsyncTaskResultStatusEnum implements EnumMessage {

    NONE("none", "未知"),
    SUCCESS("success", "全部成功"),
    PARTIAL_FAILED("partial_failed", "部分失败"),
    FAILED("failed", "全部失败"),
    TIMEOUT("timeout", "已超时"),
    CANCELED("canceled", "已取消"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    TmsAsyncTaskResultStatusEnum(String code, String name) {
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
        for (TmsAsyncTaskResultStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.name;
            }
        }
        return "";
    }

    public static TmsAsyncTaskResultStatusEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
