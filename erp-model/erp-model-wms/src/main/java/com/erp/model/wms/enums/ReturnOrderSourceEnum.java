package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReturnOrderSourceEnum {

    QC("qcInfo","质检退货", "A"),
    OTHER("other","库存退货", "B");

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

    ReturnOrderSourceEnum(String code, String name, String kingdeeCode) {
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
        for (ReturnOrderSourceEnum sourceEnum : ReturnOrderSourceEnum.values()) {
            if (code.equals(sourceEnum.getCode())) {
                return sourceEnum.getName();
            }
        }
        return "";
    }
}
