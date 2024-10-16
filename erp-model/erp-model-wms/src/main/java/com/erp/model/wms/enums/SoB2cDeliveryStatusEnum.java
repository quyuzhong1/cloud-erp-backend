package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * b2c发货单状态
 * @Author Luo_WG
 * @Date 2023/12/13 18:47
 **/
public enum SoB2cDeliveryStatusEnum implements EnumMessage {
    WAIT_HANDLE("waitHandle", "待处理"),
    PICKING("picking", "拣货中"),
    GENERATE_WAVE("generation_waves", "生成波次"),
    SHIPPED("shipped", "已发货"),
    EXCEPTION_ORDER("exceptionOrder","异常单"),
    CANCEL_DELIVERY("cancelDelivery", "取消发货");

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    SoB2cDeliveryStatusEnum(String status, String name) {
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
            for (SoB2cDeliveryStatusEnum item : SoB2cDeliveryStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SoB2cDeliveryStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(SoB2cDeliveryStatusEnum.values()).map(SoB2cDeliveryStatusEnum::getStatus).collect(Collectors.toList());
    }

    public static List<String> notShipment(){
        return Arrays.asList(WAIT_HANDLE.getCode(), EXCEPTION_ORDER.getCode());
    }
    public static List<String> notPrint(){
        return Arrays.asList(WAIT_HANDLE.getCode(), CANCEL_DELIVERY.getCode());
    }

    public static List<String> notFinishPrint(){
        return Arrays.asList(WAIT_HANDLE.getCode(), EXCEPTION_ORDER.getCode(), CANCEL_DELIVERY.getCode(), SHIPPED.getCode());
    }
}
