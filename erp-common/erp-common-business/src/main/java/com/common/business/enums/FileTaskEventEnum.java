package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileTaskEventEnum implements EnumMessage {
    //bi
    EXPORT_SKU_SALES("EXPORT_SKU_SALES", "sku销售额"),

    //plm
    PRODUCT_DETAIL_EXPORT("PRODUCT_DETAIL_EXPORT", "产品明细"),

    //wms
    INVENTORY_EXPORT("INVENTORY_EXPORT","即时库存"),
    ALIEXPRESS_DELIVERY_EXPORT("ALIEXPRESS_DELIVERY_EXPORT", "速卖通发货单"),
    B2C_DELIVERY_ORDER_EXPORT("B2C_DELIVERY_ORDER_EXPORT", "发货单导出"),
    //workflow
    PROCESS_MANAGEMENT_EXPORT("PROCESS_MANAGEMENT_EXPORT", "流程管理"),
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
