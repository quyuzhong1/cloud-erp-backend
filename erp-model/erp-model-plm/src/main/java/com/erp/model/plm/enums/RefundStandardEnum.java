package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum RefundStandardEnum implements EnumMessage {
    PURCHASE_ORDERS("purchase_orders", "以\"采购下单\"数量"),
    RECEIVING("receiving", "以\"采购收货\"数量"),
    IN_STOCK("in_stock", "以\"采购入库\"数量");
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
