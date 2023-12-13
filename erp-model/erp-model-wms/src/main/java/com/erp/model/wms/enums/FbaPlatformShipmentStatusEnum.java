package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * FBA平台货件状态
 * @Author Luo_WG
 * @Date 2023/10/31 16:05
 **/
public enum FbaPlatformShipmentStatusEnum implements EnumMessage {
    CLOSED("CLOSED", "CLOSED"),
    WORKING("WORKING", "WORKING"),
    SHIPPED("SHIPPED", "SHIPPED"),
    RECEIVING("RECEIVING", "RECEIVING"),
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

    FbaPlatformShipmentStatusEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (FbaPlatformShipmentStatusEnum item : FbaPlatformShipmentStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
