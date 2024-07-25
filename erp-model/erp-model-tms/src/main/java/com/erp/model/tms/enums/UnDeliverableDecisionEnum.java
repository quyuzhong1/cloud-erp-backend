package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 不可达处理方式
 */
public enum UnDeliverableDecisionEnum implements EnumMessage {
    RETURN("return","退回"),
    DESTROY("destroy","销毁"),
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


    UnDeliverableDecisionEnum(String code, String name) {
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
        for (UnDeliverableDecisionEnum unDeliverableDecisionEnum : UnDeliverableDecisionEnum.values()) {
            if (code.equals(unDeliverableDecisionEnum.getCode())) {
                return unDeliverableDecisionEnum.getName();
            }
        }
        return "";
    }

    public static UnDeliverableDecisionEnum getEnum(String code) {
        for (UnDeliverableDecisionEnum unDeliverableDecisionEnum : UnDeliverableDecisionEnum.values()) {
            if (code.equals(unDeliverableDecisionEnum.getCode())) {
                return unDeliverableDecisionEnum;
            }
        }
        return null;
    }
}
