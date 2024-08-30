package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocalInTransitTypeEnum implements EnumMessage {

    PURCHASE_IN_TRANSIT("PURCHASE_IN_TRANSIT", "采购在途"), TRANSFER_IN_TRANSIT("TRANSFER_IN_TRANSIT", "调拨在途");
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
