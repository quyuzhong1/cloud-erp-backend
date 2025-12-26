package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum PickingBillTypeEnum implements EnumMessage {
    B2B("B2B", "B2B订单"),
    B2C("B2C", "B2C订单"),
    FBA("FBA", "FBA头程要货单"),
    THIRD("THIRD", "三方仓头程要货单"),
    TRANSFER("transfer", "直接调拨单");
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static List<String> firstLegs(){
        return Arrays.asList(FBA.getCode(), THIRD.getCode());
    }
}
