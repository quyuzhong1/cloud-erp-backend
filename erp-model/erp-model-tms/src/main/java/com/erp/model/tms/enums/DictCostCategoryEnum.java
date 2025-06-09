package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 物流标签类型
 */
public enum DictCostCategoryEnum implements EnumMessage {

    SHIPPING_COST("shippingCost", "物流运费"),
    DECLARE_COST("declareCost", "报关费"),
    DEDUCTIBLE_TAX("deductibleTax", "可抵扣税金"),
    OTHER_TAX_FEE("otherTaxFee", "其他税费"),
    OTHER_COST("otherCost", "其他费用")
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


    DictCostCategoryEnum(String code, String name) {
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

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DictCostCategoryEnum statusEnum : DictCostCategoryEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static DictCostCategoryEnum getByCode(String code) {
        return Arrays.stream(DictCostCategoryEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}


