package com.erp.model.wms.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 速卖通发货状态枚举类
 */
@Getter
@AllArgsConstructor
public enum AliexpressDeliveryOrderStatusEnum implements EnumMessage {
    CANCELED("canceled", "已取消"),
    SIGNED("signed", "已签收"),
    TO_COUNTRY_CLEARANCE("toCountryClearance", "目的国清关完成"),
    IN_WAREHOUSE_SUCCESS("inWarehouseSuccess", "已下发仓库"),
    IN_WAREHOUSE_FAILED("inWarehouseFailed", "下发仓失败"),
    SHIPPED("shipped", "已发货"),
    SYSTEM_PROCESS_FAILED("systemProcessFailed", "系统处理失败"),
    TRANSFER_MERCHANT_WAREHOUSE_TO_SHIPMENT("merchantWarehouseShipment", "已转商家仓发货"),
    WAREHOUSE_REJECT("warehouseReject", "仓拒单"),
    SYSTEM_PROCESS("systemProcess", "系统处理中"),
    DISTRIBUTION_CENTER_DELIVERY("distributeCenterDelivery", "分拨中心出库"),
    INTERCEPT("intercept", "已拦截"),
    SUCCESS_DELIVERY("successfulDelivery", "交航成功"),
    WAREHOUSE_HANDOVER_SUCCESS("warehouseHandoverSuccess", "仓配交接成功"),
    DELIVERY_FAILED("deliveryFailed", "配送失败"),
    WAREHOUSE_RECEIVE_ORDER("warehouseReceiveOrder", "仓库已接单"),
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (AliexpressDeliveryOrderStatusEnum item : AliexpressDeliveryOrderStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (AliexpressDeliveryOrderStatusEnum item : AliexpressDeliveryOrderStatusEnum.values()) {
                if (Objects.equals(name, item.getName())) {
                    return item.getCode();
                }
            }
        }
        return CharSequenceUtil.EMPTY;
    }

    public static AliexpressDeliveryOrderStatusEnum getByCode(String code) {
        AliexpressDeliveryOrderStatusEnum[] eumnList = AliexpressDeliveryOrderStatusEnum.values();
        for (AliexpressDeliveryOrderStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
