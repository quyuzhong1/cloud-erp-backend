package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum RefundStandardEnum implements EnumMessage {
    PURCHASE_ORDERS("purchase_orders", "采购下单"),
    RECEIVING("receiving", "采购收货"),
    IN_STOCK("in_stock", "采购入库");

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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (RefundStandardEnum item : RefundStandardEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (RefundStandardEnum item : RefundStandardEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getCode();
                }
            }
        }
        return "";
    }
}
