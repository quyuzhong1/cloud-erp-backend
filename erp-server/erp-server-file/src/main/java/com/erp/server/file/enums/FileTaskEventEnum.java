package com.erp.server.file.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileTaskEventEnum implements EnumMessage {
    //plm
    PRODUCT_DETAIL_EXPORT("PRODUCT_DETAIL_EXPORT", ""),


    //wms
    INVENTORY_EXPORT("INVENTORY_EXPORT","即时库存"),
    ALIEXPRESS_DELIVERY_EXPORT("ALIEXPRESS_DELIVERY_EXPORT", ""),
    B2C_DELIVERY_ORDER_EXPORT("B2C_DELIVERY_ORDER_EXPORT", ""),
    //workflow
    PROCESS_MANAGEMENT_EXPORT("PROCESS_MANAGEMENT_EXPORT", ""),
    DEFAULT("DEFAULT", "默认");
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

}
