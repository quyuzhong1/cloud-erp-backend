package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReturnReasonEnum implements EnumMessage {
    MAINTENANCE("maintenance","保修"),
    EXPIRE("expire","过期"),
    BREAKAGE("breakage","破损"),
    QUALITY_PROBLEM("qualityProblem","质量问题"),
    UNSALABLE("unsalable","滞销"),
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


    ReturnReasonEnum(String code, String name) {
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
        for (ReturnReasonEnum reasonEnum : ReturnReasonEnum.values()) {
            if (code.equals(reasonEnum.getCode())) {
                return reasonEnum.getName();
            }
        }
        return "";
    }
}
