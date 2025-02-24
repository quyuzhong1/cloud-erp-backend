package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
/**
 * 通知规则类型
 * @Auther will
 * @Date 2025/2/12 16:13
 */
public enum CfgVirtualNoticeRuleTypeEnum implements EnumMessage {

    TOTAL("total","汇总"),
    SKU_WAREHOUSE("skuWarehouse","按“SKU+仓库”"),
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


    CfgVirtualNoticeRuleTypeEnum(String code, String name) {
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
        for (CfgVirtualNoticeRuleTypeEnum settingEnum : CfgVirtualNoticeRuleTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgVirtualNoticeRuleTypeEnum getEnum(String code) {
        for (CfgVirtualNoticeRuleTypeEnum settingEnum : CfgVirtualNoticeRuleTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
