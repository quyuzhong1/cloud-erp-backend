package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname CfgSettingEnum

 * @Date 2025-03-20 14:15
 * @Created by ZDY
 */
public enum CfgSettingEnum implements EnumMessage {

    TIME_OUT_CONFIG("timeOutConfig",  "超时设置"),
    PAY_METHOD("payMethod",  "付款方式设置"),
    AMZ_AUTH_PRE_STATE("amzAuthPreState",  "亚马逊授权state前缀"),
    DHT_CUSTOMER_WHITELIST("dhtCustomerWhitelist",  "订货通客户白名单"),

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


    CfgSettingEnum(String code, String name) {
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
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingEnum getEnum(String code) {
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
