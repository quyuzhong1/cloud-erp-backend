package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流地址类型枚举
 * @author Lambda
 * @Classname LogisticsAddressEnums
 * @Description TODO
 * @Date 2023-11-03 10:54
 * @Created by yl
 */
public enum LogisticsAddressTypeEnums implements EnumMessage {
    DELIVER("deliver","发货地址"),
    REFUND("refund","退货地址"),
    COLLECT("collect","揽收地址")
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

    LogisticsAddressTypeEnums(String code, String name){
        this.code = code;
        this.name = name;
    }

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
        for (LogisticsAddressTypeEnums typeEnums : LogisticsAddressTypeEnums.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
