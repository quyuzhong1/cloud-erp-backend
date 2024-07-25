package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 不可达处理方式
 */
public enum UnDeliverableDecisionEnum implements EnumMessage {
    RETURN(0,"退回"),
    DESTROY(1,"销毁"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private Integer code;
    /**
     * 名称
     */
    private String name;


    UnDeliverableDecisionEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getName(Integer code) {
        for (UnDeliverableDecisionEnum unDeliverableDecisionEnum : UnDeliverableDecisionEnum.values()) {
            if (code.equals(unDeliverableDecisionEnum.getCode())) {
                return unDeliverableDecisionEnum.getName();
            }
        }
        return "";
    }

    public static UnDeliverableDecisionEnum getEnum(Integer code) {
        for (UnDeliverableDecisionEnum unDeliverableDecisionEnum : UnDeliverableDecisionEnum.values()) {
            if (code.equals(unDeliverableDecisionEnum.getCode())) {
                return unDeliverableDecisionEnum;
            }
        }
        return null;
    }
}
