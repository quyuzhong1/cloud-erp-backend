package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 运费计费规则
 *
 * @author
 * @Classname ShippingFeeRuleEnums
 * @Date 2023-11-13 11:48
 * @Created by yl
 */
public enum ShippingFeeRuleEnum implements EnumMessage {

    BILLING_WEIGHT("billingWeight", "计费重"),
    NET_WEIGHT("netWeight", "实重"),
    VOLUME_WEIGHT("volumeWeight", "体积重");

    ShippingFeeRuleEnum(String code, String name) {
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
        for (ShippingFeeRuleEnum typeEnums : ShippingFeeRuleEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
