package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 新老品销售额搜索类型枚举
 * @Author Luo_WG
 * @Date 2023/9/15 11:56
 **/
public enum TargetMetricsSearchTypeEnum implements EnumMessage {
    DEPT("dept","部门"),
    USER("user","人员"),
    ;

    TargetMetricsSearchTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        TargetMetricsSearchTypeEnum[] enums = values();
        for (TargetMetricsSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return null;
    }

    public static TargetMetricsSearchTypeEnum getEnumByCode(String code) {
        TargetMetricsSearchTypeEnum[] enums = values();
        for (TargetMetricsSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
