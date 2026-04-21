package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 订单标签枚举
 */
public enum OrderFlagEnum implements EnumMessage {
    ALI_EXPRESS_STATUS("aliexpressStatus", "速卖通状态"),
    AMAZON_STATUS("amazonStatus", "亚马逊状态"),
    IS_COMBINATION_ORDER("isCombination", "组合产品"),
    FULFILLMENT_CHANNEL("fulfillmentChannel", "FBA"),
    IS_MANUAL_ORDER("isManual", "手工订单"),
    IS_INTERCEPT_ORDER("isIntercept", "拦截订单"),
    IS_SPLIT_MERGE_ORDER("refType", "合并拆分生订单类型"),
    MERGE_COUNT("mergeCount", "合并数量"),
    //（沃尔玛订单shipNodeType=WFSFulfilled或3PLFulfilled）
    SHIP_NODE_TYPE("shipNodeType", "WFS"),
    MERCADOLIBRE_MODE("mode", "美客多模式"),
    //物流类型  美客多（mode=me2 且 logistic_type = fulfillment是官方仓发货）
    LOGISTIC_TYPE("logisticType", "物流类型"),
    IS_RLATFORM_WAREHOUSE_ORDER("isPlatformWarehouseOrder", "是否平台仓订单"),
    TIKTOK_STATUS("tikTokStatus", "TikTok状态"),
    IS_REFUNDED("isRefunded", "是否退款"),
    DELIVERY_TYPE("deliveryType", "发货类型"),
    PRIORITY_LEVEL("priorityLevel", "紧急程度"),
    IS_DELIVER("isDeliver", "是否可送"),

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



    OrderFlagEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (OrderFlagEnum billTypeEnum : OrderFlagEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (OrderFlagEnum billTypeEnum : OrderFlagEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
