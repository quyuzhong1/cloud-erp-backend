package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PackingStatusEnum implements EnumMessage {
    NOT_PACKING("notPacking", "未装箱"),
    PACKING("packing", "已装箱"),
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


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PackingStatusEnum getByCode(String code) {
        PackingStatusEnum[] eumnList = PackingStatusEnum.values();
        for (PackingStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
