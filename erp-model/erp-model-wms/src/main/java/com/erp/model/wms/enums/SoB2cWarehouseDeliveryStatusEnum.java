package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * b2c三方仓发货单状态枚举
 */
public enum SoB2cWarehouseDeliveryStatusEnum implements EnumMessage {
    WAIT_HANDLE("waitHandle", "待处理"),
    SHIPPED("shipped", "已发货"),
    INTERCEPTING("intercepting", "拦截中"),
    CANCEL_DELIVERY("cancelDelivery", "取消发货")
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    SoB2cWarehouseDeliveryStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public String getStatus() {
        return status;
    }
    @Override
    public String getCode() {
        return status;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (SoB2cWarehouseDeliveryStatusEnum item : SoB2cWarehouseDeliveryStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SoB2cWarehouseDeliveryStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(SoB2cWarehouseDeliveryStatusEnum.values()).map(SoB2cWarehouseDeliveryStatusEnum::getStatus).collect(Collectors.toList());
    }
}
