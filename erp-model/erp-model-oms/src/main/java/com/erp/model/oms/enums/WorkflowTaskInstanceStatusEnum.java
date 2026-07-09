package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 任务编排实例状态
 */
public enum WorkflowTaskInstanceStatusEnum implements EnumMessage {

    RUNNING("running", "执行中"),
    WAITING("waiting", "等待中"),
    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
    CANCELLED("cancelled", "已取消"),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    WorkflowTaskInstanceStatusEnum(String code, String name) {
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

    public static WorkflowTaskInstanceStatusEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        WorkflowTaskInstanceStatusEnum statusEnum = getByCode(code);
        return statusEnum == null ? "" : statusEnum.getName();
    }
}
