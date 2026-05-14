package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * b2b发货拦截单来源类型
 */
public enum SoB2bDeliveryInterceptSourceTypeEnum implements EnumMessage {
    API("api", "API拦截单"),
    MANUAL("manual", "手工拦截单"),
    ;

    @EnumValue
    @JsonValue
    private final String status;
    private final String name;

    SoB2bDeliveryInterceptSourceTypeEnum(String status, String name) {
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
        if (StringUtils.isBlank(state)) {
            return "";
        }
        for (SoB2bDeliveryInterceptSourceTypeEnum item : values()) {
            if (state.equals(item.getStatus())) {
                return item.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(values()).map(SoB2bDeliveryInterceptSourceTypeEnum::getStatus).collect(Collectors.toList());
    }
}
