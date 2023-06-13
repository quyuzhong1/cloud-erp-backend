package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReturnReasonEnum implements EnumMessage {
    MAINTENANCE("maintenance","保修", "BX"),
    EXPIRE("expire","过期", "GQ"),
    BREAKAGE("breakage","破损", "PS"),
    QUALITY_PROBLEM("qualityProblem","质量问题", "ZLWT"),
    UNSALABLE("unsalable","滞销", "ZX"),
    OTHER("other","其他", "QT"),
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
    /**
     * 金蝶编码
     */
    private String kingdeeCode;

    ReturnReasonEnum(String code, String name, String kingdeeCode) {
        this.code = code;
        this.name = name;
        this.kingdeeCode = kingdeeCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }

    public static String getName(String code) {
        for (ReturnReasonEnum reasonEnum : ReturnReasonEnum.values()) {
            if (code.equals(reasonEnum.getCode())) {
                return reasonEnum.getName();
            }
        }
        return "";
    }

    public static ReturnReasonEnum getEnum(String code) {
        for (ReturnReasonEnum reasonEnum : ReturnReasonEnum.values()) {
            if (code.equals(reasonEnum.getCode())) {
                return reasonEnum;
            }
        }
        return null;
    }
}
