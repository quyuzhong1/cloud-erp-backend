package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.Arrays;

/**
 * 供应商的拜访类型枚举
 * @author Lambda
 * @Classname SupplierVisitEnum
 * @Description TODO
 * @Date 2023-03-21 10:09
 * @Created by yl
 */
public enum SupplierVisitEnum {

    NEW_PRODUCT("newProduct","新品"),
    ACCESS("access","准入"),
    OTHER("other","其它");


    @EnumValue
    private String type;
    private String name;

    SupplierVisitEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }


    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static SupplierVisitEnum getByStatus(String type){
        return Arrays.stream(values()).filter(a -> a.getType().equals(type))
                .findFirst().orElse(null);
    }

}
