package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * tms系统设置-费用分摊-分摊单据类型
 * @Date 2026-01-16
 * @Created jack
 */
public enum CostAllocationBillTypeEnum implements EnumMessage {
    SO_B2C( "soB2c", "B2C销售订单"),
    SO_INFO("soInfo", "B2B销售订单"),
    OTHER("other", "其他"),
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


    CostAllocationBillTypeEnum(String code, String name) {
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
        for (CostAllocationBillTypeEnum settingEnum : CostAllocationBillTypeEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CostAllocationBillTypeEnum getEnum(String code) {
        for (CostAllocationBillTypeEnum settingEnum : CostAllocationBillTypeEnum.values()) {
            if (settingEnum.getCode().equals(code)) {
                return settingEnum;
            }
        }
        return null;
    }
}
