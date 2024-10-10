package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VirtualReportTypeEnum implements EnumMessage {

    SALES_DASHBOARD("salesDashboard","销售看板"),
    REPORT_ORDER_DEMAND("reportOrderDemand","缺货统计"),
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


    VirtualReportTypeEnum(String code, String name) {
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
        for (VirtualReportTypeEnum settingEnum : VirtualReportTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static VirtualReportTypeEnum getEnum(String code) {
        for (VirtualReportTypeEnum settingEnum : VirtualReportTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
