package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 客户授信状态枚举
 */
public enum CustomerCreditStatusEnum implements EnumMessage {

    NORMAL("normal", "正常"),
    CANCELING("canceling", "取消中"),
    CANCEL("cancel", "取消"),
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


    CustomerCreditStatusEnum(String code, String name) {
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
        for (CustomerCreditStatusEnum bankTypeEnum : CustomerCreditStatusEnum.values()) {
            if (code.equals(bankTypeEnum.getCode())) {
                return bankTypeEnum.getName();
            }
        }
        return "";
    }
}
