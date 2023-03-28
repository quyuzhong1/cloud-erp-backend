package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.Arrays;

/**
 * 供应商拜访结果枚举
 *
 * @author Lambda
 * @Classname SupplierVisitResultEnumj
 * @Description TODO
 * @Date 2023-03-21 10:35
 * @Created by yl
 */
public enum SupplierVisitResultEnum {

    CONFORMITY("conformity", "合格"),
    NONCONFORMITY("nonconformity", "不合格"),
    PENDING("pending", "待定");

    @EnumValue
    private String code;
    private String name;

    SupplierVisitResultEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SupplierVisitResultEnum getByStatus(String code) {
        return Arrays.stream(values()).filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
