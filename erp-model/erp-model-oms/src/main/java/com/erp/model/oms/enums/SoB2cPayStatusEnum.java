package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author Will
 * @version 1.0
 * @description: 付款状态
 * @date 2023/8/21 11:59
 */
public enum SoB2cPayStatusEnum {

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


    SoB2cPayStatusEnum(String code, String name) {
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
        for (SoB2cPayStatusEnum soB2cPayStatusEnum : SoB2cPayStatusEnum.values()) {
            if (code.equals(soB2cPayStatusEnum.getCode())) {
                return soB2cPayStatusEnum.getName();
            }
        }
        return "";
    }

    /**
     * 通过code查询
     */
    public static SoB2cPayStatusEnum getByCode(String code){
        return Arrays.stream(SoB2cPayStatusEnum.values())
                .filter(e-> e.code.equals(code))
                .findFirst().orElse(null);
    }
}
