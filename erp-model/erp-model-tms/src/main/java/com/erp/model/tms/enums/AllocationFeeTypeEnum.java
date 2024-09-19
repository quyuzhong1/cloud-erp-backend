package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 费用分类
 */
public enum AllocationFeeTypeEnum implements EnumMessage {

    SHIPPING_COST("shippingCost", "运费"),
    DECLARE_COST("declareCost", "关税"),
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


    AllocationFeeTypeEnum(String code, String name) {
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
        for (AllocationFeeTypeEnum statusEnum : AllocationFeeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static AllocationFeeTypeEnum getByCode(String code) {
        return Arrays.stream(AllocationFeeTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}


