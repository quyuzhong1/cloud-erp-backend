package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingCompareEnum implements EnumMessage {

    HIGHER_THAN("higherThan","高于"),
    HIGHER_THAN_EQUAL("higherThanEqual","高于等于"),
    EQUAL_TO("equalTo","等于"),
    LOWER_THAN("lowerThan","低于"),
    LOWER_THAN_EQUAL("lowerThanEqual","低于等于")
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


    CfgSettingCompareEnum(String code, String name) {
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
        for (CfgSettingCompareEnum settingEnum : CfgSettingCompareEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingCompareEnum getEnum(String code) {
        for (CfgSettingCompareEnum settingEnum : CfgSettingCompareEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
