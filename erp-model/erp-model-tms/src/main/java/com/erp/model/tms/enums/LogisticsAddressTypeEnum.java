package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流地址类型枚举
 * @author Lambda
 * @Classname LogisticsAddressEnums
 * @Date 2023-11-03 10:54
 * @Created by yl
 */
public enum LogisticsAddressTypeEnum implements EnumMessage {
    DELIVER("deliver","发货地址"),
    REFUND("refund","退货地址"),
    COLLECT("collect","揽收地址"),
    TRANSFER("transfer","中转地址")
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

    LogisticsAddressTypeEnum(String code, String name){
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
        for (LogisticsAddressTypeEnum typeEnums : LogisticsAddressTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
