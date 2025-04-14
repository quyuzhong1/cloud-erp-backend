package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * @description: 开票节点枚举
 * @author: hcg
 * @date: 2025/4/14 14:38
 */
public enum InvoiceNodeEnum {
    AFTER_PULL("AfterPull", "订单拉取后"),
    AFTER_AUDIT("AfterAudit", "订单审核后"),
    NO_AUTO("NoAuto", "不自动开票"),
    ;

    /**
     * 节点类型代码
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 节点类型名称
     */
    private final String name;

    InvoiceNodeEnum(String code, String name) {
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
        for (InvoiceNodeEnum node : InvoiceNodeEnum.values()) {
            if (code.equals(node.code)) {
                return node.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        for (InvoiceNodeEnum node : values()) {
            if (Objects.equals(name, node.name)) {
                return node.code;
            }
        }
        return "";
    }
}
