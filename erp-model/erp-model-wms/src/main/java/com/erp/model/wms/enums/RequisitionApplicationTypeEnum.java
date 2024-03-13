package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequisitionApplicationTypeEnum implements EnumMessage {
    SALES_PLATFORM("salesPlatform", "FBA要货单"),
    OVERSEAS_WAREHOUSE("overseasWarehouse", "第三方仓要货单");

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

    RequisitionApplicationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (RequisitionApplicationTypeEnum typeEnum : RequisitionApplicationTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
