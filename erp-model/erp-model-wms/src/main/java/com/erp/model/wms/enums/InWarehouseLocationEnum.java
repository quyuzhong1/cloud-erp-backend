package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 上架仓位类型
 */
public enum InWarehouseLocationEnum implements EnumMessage {

    LARGE("large","推荐仓位（大货区）"),
    SMALL("small","推荐仓位（小货区）"),
    RECENT("recent","最近出入库仓位"),

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


    InWarehouseLocationEnum(String code, String name) {
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
        for (InWarehouseLocationEnum settingEnum : InWarehouseLocationEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static InWarehouseLocationEnum getEnum(String code) {
        for (InWarehouseLocationEnum settingEnum : InWarehouseLocationEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
