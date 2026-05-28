package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReturnDetailTypeEnum implements EnumMessage {

    SKU("sku", "单个sku退货"),
    PACK("pack", "整箱退货");

    private String code;
    private String name;

    public static String getName(String code) {
        for (ReturnDetailTypeEnum typeEnum : ReturnDetailTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
