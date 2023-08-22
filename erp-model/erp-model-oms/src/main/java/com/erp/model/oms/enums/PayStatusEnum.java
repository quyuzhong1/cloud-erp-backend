package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 付款状态
 * @date 2023/8/21 11:59
 */
public enum PayStatusEnum {

    ENUM_PAYMENT("payment",  "待付款"),
    ENUM_PAID("paid",  "已付款"),

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


    PayStatusEnum(String code, String name) {
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
        for (PayStatusEnum payStatusEnum : PayStatusEnum.values()) {
            if (code.equals(payStatusEnum.getCode())) {
                return payStatusEnum.getName();
            }
        }
        return "";
    }
}
