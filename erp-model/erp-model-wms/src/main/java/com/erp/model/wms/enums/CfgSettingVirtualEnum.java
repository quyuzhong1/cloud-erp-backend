package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingVirtualEnum implements EnumMessage {

    SALES_DASHBOARD("salesDashboard","销售看板"),
    VIRTUAL_RULE("virtualRule","规则设置"),
    INVENTORY_AGE_STATISTICS("INVENTORY_AGE_STATISTICS","库龄统计"),
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


    CfgSettingVirtualEnum(String code, String name) {
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
        for (CfgSettingVirtualEnum settingEnum : CfgSettingVirtualEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingVirtualEnum getEnum(String code) {
        for (CfgSettingVirtualEnum settingEnum : CfgSettingVirtualEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
