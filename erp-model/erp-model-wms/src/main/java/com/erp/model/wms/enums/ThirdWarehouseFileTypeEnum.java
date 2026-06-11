package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;

/**
 * 三方仓上传文件类型。
 */
public enum ThirdWarehouseFileTypeEnum implements EnumMessage {

    ORDER_ATTACHMENT("ORDER_ATTACHMENT", "订单附件"),
    ORDER_PACKING_ATTACHMENT("ORDER_PACKING_ATTACHMENT", "订单装箱附件"),
    SHIPMENT_LABEL_ATTACHMENT("SHIPMENT_LABEL_ATTACHMENT", "货件标签附件"),
    ;

    private final String code;
    private final String name;

    ThirdWarehouseFileTypeEnum(String code, String name) {
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
}
