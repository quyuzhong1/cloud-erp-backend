package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: B2C销售订单异常类型枚举
 * @date 2023/8/22 12:27
 */
public enum SoB2cAbnormalTypeEnum {

    ENUM_APPROVE_REJECT("approveReject",  "订单规则审核不通过"),
    ENUM_DISTRIBUTION_REJECT("distributionReject",  "配货规则匹配失败"),
    ENUM_MANUAL_REJECT("manualReject",  "人工审核不通过"),
    ENUM_RATE_NOT_EXIST_REJECT("rateNotExistReject",  "汇率配置不存在"),

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


    SoB2cAbnormalTypeEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum : SoB2cAbnormalTypeEnum.values()) {
            if (code.equals(soB2cAbnormalTypeEnum.getCode())) {
                return soB2cAbnormalTypeEnum.getName();
            }
        }
        return "";
    }
}
