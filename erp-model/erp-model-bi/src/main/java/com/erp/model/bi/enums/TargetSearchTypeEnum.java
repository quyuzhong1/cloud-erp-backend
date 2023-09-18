package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Will
 * @version 1.0
 * @description: 目标完成查看类型
 * @date 2023/9/14 17:36
 */
public enum TargetSearchTypeEnum implements EnumMessage {

    DEPT("dept","二级部门"),
    USER("user","人员"),
    SHOP("shop","店铺"),
    CATEGORY("category","品类"),
    SKU("sku","SKU"),
    ;


    TargetSearchTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 通过Code查找
     */
    public static String getByCode(String code) {
        return Arrays.stream(TargetSearchTypeEnum.values())
                .filter(e-> e.getCode().equals(code))
                .findFirst()
                .flatMap(obj -> Optional.ofNullable(obj.getName()))
                .orElse("");
    }
}
