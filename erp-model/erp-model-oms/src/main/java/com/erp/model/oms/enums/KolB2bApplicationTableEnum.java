package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum KolB2bApplicationTableEnum implements EnumMessage {


    ALL("all", "全部"),
    TO_BE_APPROVE("toBeApprove", "待我审核"),
    APPROVE("approve", "审核通过"),
    WAIT_SHIPPED("waitShipped", "待发货"),
    SHIPPED("shipped", "已发货"),
    REJECT("reject", "不通过"),
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


    KolB2bApplicationTableEnum(String code, String name) {
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
        for (KolB2bApplicationTableEnum billTypeEnum : KolB2bApplicationTableEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (KolB2bApplicationTableEnum billTypeEnum : KolB2bApplicationTableEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
