package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 费用计算费用名称
 * @author Will
 * @date: 2023/11/14 18:01
 */
public enum ShippingOtherCostNameEnum implements EnumMessage {

    DISCOUNT_COST("discountCost", "折扣费"),
    OVERSIZE_SUURCHARGE_COST("oversizeSurchargeCost", "超尺寸附加费"),
    SIGNATURE_COST("signatureCost", "签名费"),
    FUEL_SURCHARGE_COST("fuelSurchargeCost", "燃油附加费"),
    PREMIUM_COST("premiumCost", "保险费")
    ;

    ShippingOtherCostNameEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
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
        for (ShippingOtherCostNameEnum typeEnums : ShippingOtherCostNameEnum.values()) {
            if (typeEnums.getCode().equals(code)) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
