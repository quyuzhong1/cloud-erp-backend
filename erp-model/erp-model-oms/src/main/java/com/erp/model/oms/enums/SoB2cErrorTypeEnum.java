package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @version 1.0
 * @description: B2C销售订单作废类型枚举
 * @date 2023/12/20 10:27
 */
public enum SoB2cErrorTypeEnum {

    SUBMIT_DELIVERY("submitDelivery",  "提交发货异常"),
    SIGN_DELIVERY("signDelivery",  "标记发货异常"),
    GET_LOGISTICS_CODE("getLogisticsCode",  "获取物流单异常"),
    GENERATE_OUTSTOCK("generateOutstock",  "生成销售出库单"),
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


    SoB2cErrorTypeEnum(String code, String name) {
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
        for (SoB2cErrorTypeEnum typeEnum : SoB2cErrorTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
