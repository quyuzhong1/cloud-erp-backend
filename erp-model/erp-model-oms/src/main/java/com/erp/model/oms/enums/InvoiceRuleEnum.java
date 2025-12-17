package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * @description: 开票规则
 * @author: hcg
 * @date: 2025/4/14 14:21
 */
public enum InvoiceRuleEnum implements EnumMessage {
    AMOUNT("Amount", "按产品全额开票"),
    CUSTOM("Custom", "按（产品全额×自定义百分比）后开票"),
    DEDUCT("Deduct", "按（产品全额-佣金）后开票"),
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

    InvoiceRuleEnum(String code, String description) {
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
        for (InvoiceRuleEnum rule : InvoiceRuleEnum.values()) {
            if (code.equals(rule.getCode())) {
                return rule.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String description) {
        for (InvoiceRuleEnum rule : InvoiceRuleEnum.values()) {
            if(Objects.equals(description, rule.name)) {
                return rule.code;
            }
        }
        return "";
    }
}
