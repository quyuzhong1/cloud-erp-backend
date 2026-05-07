package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 报关单批量更新字段枚举（主表）
 */
public enum TmsDeclareBillBatchFieldEnum implements EnumMessage {
    SOURCE_CODE("source_code", "来源单号"),
    BUSINESS_TYPE("business_type", "发货类型"),
    COUNTRY_NAME("country_name", "目的国家名称"),
    DECLARE_DATE("declare_date", "报关日期"),
    DECLARE_TYPE("declare_type", "报关类型"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    TmsDeclareBillBatchFieldEnum(String code, String name) {
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

    public static TmsDeclareBillBatchFieldEnum getEnumByCode(String code) {
        for (TmsDeclareBillBatchFieldEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
