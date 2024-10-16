package com.erp.model.wms.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReturnModeEnum implements EnumMessage {

    DEDUCTION("deduction","退货扣款", "B"),
    REPLENISHMENT("replenishment","退货补货", "A");

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
     * 金蝶编号
     */
    private String kingdeeCode;


    ReturnModeEnum(String code, String name, String kingdeeCode) {
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
        if (StrUtil.isBlank(code)){
            return "";
        }
        for (ReturnModeEnum returnModeEnum : ReturnModeEnum.values()) {
            if (code.equals(returnModeEnum.getCode())) {
                return returnModeEnum.getName();
            }
        }
        return "";
    }
}
