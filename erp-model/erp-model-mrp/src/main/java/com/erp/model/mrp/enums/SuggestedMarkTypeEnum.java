package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuggestedMarkTypeEnum implements EnumMessage {

    /**
     * 按节奏采购
     */
    NORMAL_PURCHASE("NORMAL_PURCHASE","按节奏采购"),
    /**
     * 需采购-加急
     */
    RISKS_PURCHASE("RISKS_PURCHASE","需采购-加急"),
    /**
     * 需采购-必断货
     */
    WILL_OUT_OF_STOCK_PURCHASE("WILL_OUT_OF_STOCK_PURCHASE","需采购-必断货"),
    /**
     * 需采购-已断货
     */
    OUT_OF_STOCK_PURCHASE("OUT_OF_STOCK_PURCHASE","需采购-已断货"),
    /**
     * 按节奏发货
     */
    NORMAL_DELIVERY("NORMAL_DELIVERY","按节奏发货"),
    /**
     * 需发货-加急
     */
    RISKS_DELIVERY("RISKS_DELIVERY","需发货-加急"),
    /**
     * 需发货-必断货
     */
    WILL_OUT_OF_STOCK_DELIVERY("WILL_OUT_OF_STOCK_DELIVERY","需发货-必断货"),
    /**
     * 需发货-已断货
     */
    OUT_OF_STOCK_DELIVERY("OUT_OF_STOCK_DELIVERY","需发货-已断货"),
    /**
     * 按节奏
     */
    NORMAL("NORMAL","按节奏"),
    /**
     * 有风险
     */
    RISKS("RISKS","有风险"),
    /**
     * 必断货
     */
    WILL_OUT_OF_STOCK("WILL_OUT_OF_STOCK","必断货"),
    /**
     * 已断货
     */
    OUT_OF_STOCK("OUT_OF_STOCK","已断货");



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
