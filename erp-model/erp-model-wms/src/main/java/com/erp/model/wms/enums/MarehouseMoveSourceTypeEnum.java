package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MarehouseMoveSourceTypeEnum implements EnumMessage {

    FIRST_MILE_PICKING("firstMilePicking", "头程拣货单"),
    B2B_PICKING("b2bPicking", "B2B拣货单");

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

    MarehouseMoveSourceTypeEnum(String code, String name) {
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

    public static String getNameByCode(String code){
        for (MarehouseMoveSourceTypeEnum typeEnum : MarehouseMoveSourceTypeEnum.values()) {
            if(typeEnum.getCode().equals(code)){
                return typeEnum.getName();
            }
        }
        return "";
    }
}
