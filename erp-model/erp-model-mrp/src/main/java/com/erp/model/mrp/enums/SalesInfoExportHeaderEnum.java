package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SalesInfoExportHeaderEnum implements EnumMessage {
    PLATFORM("platform", "平台"),
    SHOP_NAME("shopName", "店铺"),
    SKU_NO("skuNo", "SKU"),
    PRODUCT_NAME("productName", "产品名称"),
    TYPE_NAME("typeName", "类型"),
    ;

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static SalesInfoExportHeaderEnum getEnum(String code) {
        for (SalesInfoExportHeaderEnum typeEnum : SalesInfoExportHeaderEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
