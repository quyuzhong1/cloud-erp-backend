package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 费用名称
 * @date 2023/11/8 10:31
 */
public enum ShippingCostNameEnum implements EnumMessage {

    DISCOUNT_RATE("discountRate","折扣费率","cost","%"),
    OVERSIZE_SURCHARGE_COST("oversizeSurchargeCost","超尺寸附加费","side","CNY"),
    SIGNATURE_COST("signatureCost","签名费","vote","CNY"),
    FUEL_SURCHARGE_RATE("fuelSurchargeRate","燃油附加费率","cost","%"),
    PREMIUM_COST("premiumCost","保险费","vote","CNY")
    ;

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

    /**
     * 类型
     */
    private String type;

    /**
     * 单位
     */
    private String unit;

    ShippingCostNameEnum(String code, String name,String type,String unit){
        this.code = code;
        this.name = name;
        this.type = type;
        this.unit = unit;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getUnit() {
        return this.unit;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShippingCostNameEnum typeEnums : ShippingCostNameEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
