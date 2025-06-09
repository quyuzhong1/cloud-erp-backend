package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecentSuggestionEnum implements EnumMessage {
    /**
     * 最近断货
     */
    RECENT_OUT_OF_STOCK("RECENT_OUT_OF_STOCK","最近断货"),
    /**
     * 最近采购
     */
    RECENT_PURCHASE("RECENT_PURCHASE","最近采购"),
    /**
     * 最近发货
     */
    RECENT_DELIVERY("RECENT_DELIVERY","最近发货");

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
}
