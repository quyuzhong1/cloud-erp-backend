package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * @description: 税费类型枚举
 * @author: hcg
 * @date: 2025/4/14 14:28
 */
public enum TaxTypeEnum {
    PURCHASE_SALE("PurchaseSale", "采购经销"),
    SELF_SALE("SelfSale", "自产自销"),
    ;

    /**
     * 税费类型编码
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 税费类型描述
     */
    private final String name;

    TaxTypeEnum(String code, String name) {
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
        for (TaxTypeEnum type : TaxTypeEnum.values()) {
            if (code.equals(type.getCode())) {
                return type.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        for (TaxTypeEnum type : TaxTypeEnum.values()) {
            if (Objects.equals(name, type.getName())) {
                continue;
            }
            return type.getCode();
        }
        return "";
    }
}
