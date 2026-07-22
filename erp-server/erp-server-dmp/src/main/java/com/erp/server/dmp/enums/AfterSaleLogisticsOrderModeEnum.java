package com.erp.server.dmp.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 寄修物流下单方式
 */
@Getter
public enum AfterSaleLogisticsOrderModeEnum {

    PLATFORM("platform", "下单至物流平台"),
    MANUAL("manual", "自行寄出"),
    ;

    @JsonValue
    private final String code;
    private final String name;

    AfterSaleLogisticsOrderModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AfterSaleLogisticsOrderModeEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return PLATFORM;
        }
        return Arrays.stream(values())
                .filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
