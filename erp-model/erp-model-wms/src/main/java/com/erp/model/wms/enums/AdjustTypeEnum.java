package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum AdjustTypeEnum implements EnumMessage {
    LOAD("load", "装入"),
    PRETEND("pretend", "装出"),
    REPACKING("repacking", "重新装箱"),
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
            for (AdjustTypeEnum item : AdjustTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static AdjustTypeEnum getByCode(String code) {
        AdjustTypeEnum[] eumnList = AdjustTypeEnum.values();
        for (AdjustTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
