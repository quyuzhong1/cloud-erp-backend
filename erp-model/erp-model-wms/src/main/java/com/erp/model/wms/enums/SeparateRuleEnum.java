package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 盘点拆单规则枚举
 * @author Cloud
 */
public enum SeparateRuleEnum implements EnumMessage {


    WAREHOUSE_LOCATION("warehouseAndLocation", "按仓库+仓位"),
    WAREHOUSE("warehouse", "按仓库"),
    WAREHOUSE_AREA("warehouseAndArea", "按仓库+区域"),
    LOCATION_SKU("locationAndSku", "按仓位+SKU");

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

    SeparateRuleEnum(String code, String name) {
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

    public static SeparateRuleEnum getByCode(String code) {
        return Arrays.stream(SeparateRuleEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
