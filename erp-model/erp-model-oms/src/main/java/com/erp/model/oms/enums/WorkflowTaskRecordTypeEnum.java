package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * WorkflowTaskRecord来源类型
 */
public enum WorkflowTaskRecordTypeEnum implements EnumMessage {
    EXHIBITION_ORDER_APPROVE("exhibitionOrderApprove", "展会订单审核"),
    EXHIBITION_ORDER_DISAPPROVE("exhibitionOrderDisapprove", "展会订单反审核"),
    SO_B2C_GET_LOGISTICS("soB2cGetLogistics", "获取物流单"),
    PACKAGE_PLAN_GENERATE("packagePlanGenerate", "生成组包计划"),
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


    WorkflowTaskRecordTypeEnum(String code, String name) {
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
        for (WorkflowTaskRecordTypeEnum billTypeEnum : WorkflowTaskRecordTypeEnum.values()) {
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
        for (WorkflowTaskRecordTypeEnum billTypeEnum : WorkflowTaskRecordTypeEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }

    public static WorkflowTaskRecordTypeEnum getByName(String name) {
        WorkflowTaskRecordTypeEnum[] values = values();
        for (WorkflowTaskRecordTypeEnum value : values) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
