package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum LogisticsLabelStatusEnum implements EnumMessage {

    OBTAINED("obtained", "已获取"),
    NOT_OBTAINED("notObtained", "未获取"),
    OBTAIN_FAILED("obtainFailed", "获取失败"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsLabelStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (LogisticsLabelStatusEnum item : LogisticsLabelStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
