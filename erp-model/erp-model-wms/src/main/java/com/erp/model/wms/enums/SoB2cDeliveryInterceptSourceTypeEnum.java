package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * b2c发货拦截单状态
 */
public enum SoB2cDeliveryInterceptSourceTypeEnum implements EnumMessage {
    SO_B2C("soB2c", "人工拦截单"),
    API("api", "api拦截单"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    SoB2cDeliveryInterceptSourceTypeEnum(String status, String name) {
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
            for (SoB2cDeliveryInterceptSourceTypeEnum item : SoB2cDeliveryInterceptSourceTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SoB2cDeliveryInterceptSourceTypeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(SoB2cDeliveryInterceptSourceTypeEnum.values()).map(SoB2cDeliveryInterceptSourceTypeEnum::getStatus).collect(Collectors.toList());
    }
}
