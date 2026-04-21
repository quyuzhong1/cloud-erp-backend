package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * b2b发货拦截单处理状态
 */
public enum SoB2bDeliveryInterceptStatusEnum implements EnumMessage {
    WAIT_HANDLE("waitHandle", "待处理"),
    HANDLE("handle", "已处理"),
    ;

    @EnumValue
    @JsonValue
    private final String status;
    private final String name;

    SoB2bDeliveryInterceptStatusEnum(String status, String name) {
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

    public static String getName(String status) {
        if (StringUtils.isNotBlank(status)) {
            for (SoB2bDeliveryInterceptStatusEnum item : values()) {
                if (status.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SoB2bDeliveryInterceptStatusEnum getByStatus(String status) {
        return Arrays.stream(values()).filter(item -> item.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(values()).map(SoB2bDeliveryInterceptStatusEnum::getStatus).collect(Collectors.toList());
    }
}
