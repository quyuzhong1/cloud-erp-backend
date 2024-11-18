package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgSettingEnum implements EnumMessage {
    NEW_DAYS("newDays", "新品天数"),
    REPLENISHMENT_DAYS("replenishment_days","补货天数"),
    CALCULATION_DAYS("calculation_days","计算天数"),
    ;

    /**
     * 类型
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingEnum getEnum(String code) {
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}

