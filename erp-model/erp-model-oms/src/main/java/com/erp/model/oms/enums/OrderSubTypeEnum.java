package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public enum OrderSubTypeEnum implements EnumMessage {
    OFFLINE_ORDER("100.30.01", "线下订单"),
    ONLINE_ORDER("100.20.01", "线上订单"),
    GIFT_ORDER("100.20.02", "赠品订单"),
    GIFT_REPLENISHMENT("100.20.03", "赠品补发"),
    INFLUENCER_SAMPLE("100.20.04", "红人样品"),
    DEPARTMENT_USAGE("100.20.05", "部门领用"),
    REVIEW_CASHBACK("100.20.06", "评价返现"),
    COMPENSATION_COUPON("100.20.07", "补偿赠券"),
    LUCKY_BAG_DRAW("100.20.08", "福袋抽奖"),
    EXCHANGE_REPLACEMENT("100.20.09", "换货补发"),
    REPLENISHMENT("100.20.10", "补发"),
    OTHER("100.20.11", "其他"),
    ;
    OrderSubTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public final String code;
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

    /**
     * 通过code查询
     */
    public static OrderSubTypeEnum getByCode(String code){
        return Stream.of(OrderSubTypeEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (OrderSubTypeEnum statusEnum : OrderSubTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }


}
