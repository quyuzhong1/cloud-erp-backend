package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * mq消费类型
 * @author jack
 * @date 2025-11-17
 */
public enum MyConsumerRecordTypeEnum implements EnumMessage {
    SYNC_FS("syncFs", "ERP审批同步回调"),
    SYNC_COMMENT("syncComment", "ERP审批日志回调"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    MyConsumerRecordTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (MyConsumerRecordTypeEnum state : MyConsumerRecordTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static MyConsumerRecordTypeEnum getByCode(String code) {
        for (MyConsumerRecordTypeEnum state : MyConsumerRecordTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<MyConsumerRecordTypeEnum> getAll() {
        return Arrays.stream(MyConsumerRecordTypeEnum.values()).collect(Collectors.toList());
    }
}
