package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: 物流运输方式
 * @author Will
 * @date: 2024/4/8 9:50
 */
public enum LogisticsTransportTypeEnum implements EnumMessage {
    EXPRESS_DELIVERY("expressDelivery","快递"),
    OCEAN("ocean","海运"),
    AVIATION("aviation","空运"),
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

    LogisticsTransportTypeEnum(String code, String name){
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
        for (LogisticsTransportTypeEnum typeEnums : LogisticsTransportTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
