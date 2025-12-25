package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum ShipmentOrderTypeEnum implements EnumMessage {
    FBA("fba", "FBA"),
    AWD("awd", "AWD"),
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ShipmentOrderTypeEnum item : ShipmentOrderTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ShipmentOrderTypeEnum getByCode(String code) {
        ShipmentOrderTypeEnum[] eumnList = ShipmentOrderTypeEnum.values();
        for (ShipmentOrderTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
