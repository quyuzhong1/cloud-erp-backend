package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeclareTransactionMethodEnum implements EnumMessage {
    EXW("EXW","EXW"),
    CIF("CIF","CIF"),
    C_AND_F("C&F","C&F"),
    FOB("FOB","FOB"),
    C_AND_I("C&I","C&I"),
    MARKET_PRICE("marketPrice","市场价"),
    CUSHION_COMPARTMENT("cushionCompartment","垫仓"),
    ;


    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    DeclareTransactionMethodEnum(String code, String name) {
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
