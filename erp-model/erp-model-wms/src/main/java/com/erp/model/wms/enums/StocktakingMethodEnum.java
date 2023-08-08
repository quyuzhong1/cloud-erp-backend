package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 盘点方式枚举
 * @author Cloud
 */
public enum StocktakingMethodEnum implements EnumMessage {


    OPEN_COUNT("openCount", "明盘"),
    BLIND_COUNT("blindCount", "盲盘");

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

    StocktakingMethodEnum(String code, String name) {
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

    public static StocktakingMethodEnum getByCode(String code) {
        return Arrays.stream(StocktakingMethodEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
