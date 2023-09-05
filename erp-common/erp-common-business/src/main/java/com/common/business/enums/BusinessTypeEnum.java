package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 平台类型
 *
 * @Author Cloud
 * @Date 2023/8/28 10:47
 **/
public enum BusinessTypeEnum implements EnumMessage {
    // 销售订单
    ORDER("order","销售订单"),
    REFUND("refund","退款单"),
    PRODUCT("product","商品"),
    RETURN("return","退货单"),
    DELIVERY("delivery","发货单"),
    AUTH("auth","授权"),
    REFRESH_TOKEN("refresh_token","刷新token"),
    ;

    @JsonValue
    @EnumValue
    private String code;

    private String name;


    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    BusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BusinessTypeEnum getByCode(String code) {
        for (BusinessTypeEnum state : BusinessTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }
}
