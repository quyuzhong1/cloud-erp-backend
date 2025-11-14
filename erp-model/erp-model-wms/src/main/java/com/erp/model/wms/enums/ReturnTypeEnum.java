package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum ReturnTypeEnum implements EnumMessage {

    DEDUCTION("refund","退货退款"),
    REPLENISHMENT("replenishment","退货补货"),
	CUSTOMER_RETURNS("customerReturns","买家退货"),
    RETURNS_FROM_SERVICE_PROVIDERS("returnsFromServiceProviders","服务商退件"),
    CLAIM("claim","认领"),
    OTHER("other","其他"),
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


    ReturnTypeEnum(String code, String name) {
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
        for (ReturnTypeEnum returnTypeEnum : ReturnTypeEnum.values()) {
            if (code.equals(returnTypeEnum.getCode())) {
                return returnTypeEnum.getName();
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if(StringUtils.isBlank(name)) {
            return "";
        }
        for (ReturnTypeEnum returnTypeEnum : ReturnTypeEnum.values()) {
            if (name.equals(returnTypeEnum.getName())) {
                return returnTypeEnum.getCode();
            }
        }
        return "";
    }
}
