package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * PDA采购入库单质检状态
 */
public enum PdaQclStatusEnum {
    WAIT_QC("waitQc", "未质检"),
    PARTIAL_QC("partialQc", "部分质检"),
    FINISH_QC("finishQc", "已质检"),
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


    PdaQclStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PdaQclStatusEnum getByCode(String code) {
        PdaQclStatusEnum[] eumnList = PdaQclStatusEnum.values();
        for (PdaQclStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
