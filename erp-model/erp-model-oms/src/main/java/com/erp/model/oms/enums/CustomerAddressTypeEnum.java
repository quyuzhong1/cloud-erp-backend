package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Administrator
 */

public enum CustomerAddressTypeEnum {
    FORWARDER("forwarder","货代地址"),
    DELIVER("receive","收货地址"),
    COMPANY("company","公司地址"),
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


    CustomerAddressTypeEnum(String code, String name) {
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
        for (CustomerAddressTypeEnum addressTypeEnum : CustomerAddressTypeEnum.values()) {
            if (code.equals(addressTypeEnum.getCode())) {
                return addressTypeEnum.getName();
            }
        }
        return "";
    }
}
