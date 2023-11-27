package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Arrays;

/**
 * 【海外仓入库单】
 * 入库单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OverseasInstockStatusEnum implements EnumMessage {
    TO_BE_SHIPPED("toBeShipped", "待发货"),
    TO_BE_SIGNED("toBeSigned", "待签收"),
    PARTIAL_SIGNED("partialSigned", "部分签收"),
    SIGNED("signed", "已签收"),
    CANCELED("canceled", "已取消"),
    ABNORMAL("abnormal", "异常");

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (OverseasInstockStatusEnum item : OverseasInstockStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static OverseasInstockStatusEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }

}
