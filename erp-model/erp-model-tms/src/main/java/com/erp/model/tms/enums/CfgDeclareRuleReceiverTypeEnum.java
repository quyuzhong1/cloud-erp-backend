package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum CfgDeclareRuleReceiverTypeEnum implements EnumMessage {
    BY_COMPANY("byCompany","按核算公司"),
    BY_CUSTOMER("byCustomer","按客户"),
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


    CfgDeclareRuleReceiverTypeEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (BusinessTypeEnum typeEnums : BusinessTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
