package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:发货类型
 * @author Will
 * @date: 2024/3/22 16:29
 */
public enum ShipmentTypeEnum implements EnumMessage {

    SELF_DELIVER("selfDeliver", "自发货"),
    THIRD_WAREHOUSE_DELIVER("thirdWarehouseDeliver", "第三方仓发货"),
    PLATFORM_DELIVER("platformDeliver", "平台仓发货"),
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


    ShipmentTypeEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShipmentTypeEnum statusEnum : ShipmentTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}


