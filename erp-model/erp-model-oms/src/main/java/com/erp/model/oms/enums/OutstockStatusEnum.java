package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * @description: 系统是否已出库(none未出库part部分出库all已出库)
 * @author: hcg
 * @date: 2025/4/14 14:21
 */
public enum OutstockStatusEnum implements EnumMessage {
    NONE("none", "未出库"),
    PART("part", "部分出库"),
    ALL("all", "已出库"),
    ;

    /**
     * 开票类型
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 开票规则描述
     */
    private final String name;

    OutstockStatusEnum(String code, String description) {
        this.code = code;
        this.name = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (OutstockStatusEnum rule : OutstockStatusEnum.values()) {
            if (code.equals(rule.getCode())) {
                return rule.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String description) {
        for (OutstockStatusEnum rule : OutstockStatusEnum.values()) {
            if(Objects.equals(description, rule.name)) {
                return rule.code;
            }
        }
        return "";
    }
}
