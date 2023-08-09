package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 盘点类型枚举
 * @author Cloud
 */
public enum StocktakingTypeEnum implements EnumMessage {


    BY_WAREHOUSE("byWarehouse", "按仓库盘点"),
    BY_LOCATION("byLocation", "按仓位盘点"),
    BY_SKU("bySku", "按SKU盘点"),

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

    StocktakingTypeEnum(String code, String name) {
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

    public static StocktakingTypeEnum getByCode(String code) {
        return Arrays.stream(StocktakingTypeEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
