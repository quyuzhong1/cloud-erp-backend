package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单据类型
 */
public enum ThirdDeliveryStatusEnum implements EnumMessage {
    CREATING("creating", "创建中"),
    WAIT_SHIPPED("waitShipped", "待发货"),
    SHIPPED("shipped", "已发货"),
    INTERCEPTING("intercepting", "拦截中"),
    FAILED("failed", "创建失败"),
    CANCEL_DELIVERY("cancelDelivery", "取消发货"),
    EXCEPTION_ORDER("exceptionOrder", "异常订单"),
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



    ThirdDeliveryStatusEnum(String code, String name) {
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


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ThirdDeliveryStatusEnum billTypeEnum : ThirdDeliveryStatusEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }
}
