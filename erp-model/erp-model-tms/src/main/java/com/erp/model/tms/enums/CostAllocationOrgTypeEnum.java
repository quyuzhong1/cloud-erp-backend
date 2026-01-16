package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * tms系统设置-费用分摊-组织类型枚举
 * @Date 2026-01-16
 * @Created jack
 */
public enum CostAllocationOrgTypeEnum implements EnumMessage {
    BILL_ORG("billOrg","单据成本组织"),
    LOGISTICS_SUPPLIER_ORG("logisticsSupplierOrg","物流组织"),
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


    CostAllocationOrgTypeEnum(String code, String name) {
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
        for (CostAllocationOrgTypeEnum settingEnum : CostAllocationOrgTypeEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CostAllocationOrgTypeEnum getEnum(String code) {
        for (CostAllocationOrgTypeEnum settingEnum : CostAllocationOrgTypeEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum;
            }
        }
        return null;
    }
}
