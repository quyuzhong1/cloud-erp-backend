package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingEnum implements EnumMessage {
    LOGISTICS_PRODUCT_DEST_DECLARE_PRICE("logisticsProductDestDeclarePrice","物流产品信息-目的国申报价"),
    NOTIC("notic","通知管理"),
    RECONCILIATION_CYCLE("reconciliationCycle","生成设置"),
    BILL_AUTO_ADD("billAutoAdd","单据生成"),
    ALLOCATION_SETTING("allocationSetting","分摊设置"),

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


    CfgSettingEnum(String code, String name) {
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
