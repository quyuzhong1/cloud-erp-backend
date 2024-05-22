package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @date 2024/5/16 18:46
 */
public enum RuleToDeclarePriceTypeEnum  implements EnumMessage {

    FIXED_PRICE("fixedPrice","按固定价格申报"),
    PRICE_PERCENTAGE("pricePercentage","按价格百分比申报")
    ;

    RuleToDeclarePriceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 标识
     */
    @EnumValue
    @JsonValue
    private String code;
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


    public static String getName(String code) {
        for (RuleToDeclarePriceTypeEnum statusTypeEnum : RuleToDeclarePriceTypeEnum.values()) {
            if (code.equals(statusTypeEnum.getCode())) {
                return statusTypeEnum.getName();
            }
        }
        return "";
    }
}
