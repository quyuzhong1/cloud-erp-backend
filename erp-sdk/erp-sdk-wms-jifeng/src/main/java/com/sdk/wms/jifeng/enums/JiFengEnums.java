package com.sdk.wms.jifeng.enums;


import lombok.Getter;

@Getter
public enum JiFengEnums {
    CONTAIN_BATTERY("containBattery",GoodsAttributeEnum.class),
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    JiFengEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }

    /**
     * 货物属性枚举
     */
    @Getter
    public enum GoodsAttributeEnum {
        GENERAL_CARGO(0,"普货"),
        INCLUDING_BATTERY(1,"含电池"),
        PURE_BATTERY(2,"纯电池"),
        TEXTILE(3,"纺织品"),
        FRAGILE_PRODUCTS(4,"易碎品"),
        EXCEEDING_STANDARD_PURE_BATTERIES(6,"超标纯电池"),
        EXCEEDING_STANDARD_WITH_BATTERIES(7,"超标含电池")
        ;
        private final Integer code;
        private final String name;
        GoodsAttributeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

}
