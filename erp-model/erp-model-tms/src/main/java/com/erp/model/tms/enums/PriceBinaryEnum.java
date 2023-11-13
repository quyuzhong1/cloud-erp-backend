package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 价格进制
 * @date 2023/11/8 17:27
 */
public enum PriceBinaryEnum implements EnumMessage {

    TWO_DECIMAL_PLACES("twoDecimalPlaces","0.01  保留两位小数四舍五入"),
    ONE_DECIMAL_PLACES("oneDecimalPlaces","0.1  保留一位小数四舍五入"),
    NO_DECIMALS("noDecimals","0  向下取整，小数舍弃"),
    BINARY("binary","0.5  0.5进制"),
    ROUND_UP("roundUp","1  向上取整,小数进1"),
    PRESERVE_INTEGERS("preserveIntegers","2  保留整数,四舍五入"),

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

    PriceBinaryEnum(String code, String name){
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

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (LogisticsAddressTypeEnum typeEnums : LogisticsAddressTypeEnum.values()) {
            if (name.equals(typeEnums.getName())) {
                return typeEnums.getCode();
            }
        }
        return "";
    }
}
