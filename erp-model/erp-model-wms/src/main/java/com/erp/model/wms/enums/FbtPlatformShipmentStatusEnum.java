package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * FBT平台货件状态
 *
 * 对齐 TikTok FBT 接口 order_status 枚举，名称与状态码保持一致，避免和 FBA 状态集混用。
 */
public enum FbtPlatformShipmentStatusEnum implements EnumMessage {
    WORKING("WORKING", "WORKING"),
    READY_TO_SHIP("READY_TO_SHIP", "READY_TO_SHIP"),
    TO_BE_RECEIVED("TO_BE_RECEIVED", "TO_BE_RECEIVED"),
    ARRIVED_HUB("ARRIVED_HUB", "ARRIVED_HUB"),
    INTERNAL_TRANSFER("INTERNAL_TRANSFER", "INTERNAL_TRANSFER"),
    RECEIVING("RECEIVING", "RECEIVING"),
    PARTIALLY_RECEIVED("PARTIALLY_RECEIVED", "PARTIALLY_RECEIVED"),
    RECEIVED("RECEIVED", "RECEIVED"),
    CANCELLED("CANCELLED", "CANCELLED"),
    DELIVERED("DELIVERED", "DELIVERED"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    FbtPlatformShipmentStatusEnum(String code, String name) {
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
            for (FbtPlatformShipmentStatusEnum item : FbtPlatformShipmentStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
