package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * 产品开发状态
 */
public enum ProductDetailStateEnum implements EnumMessage {
    NO_DEVELOP(1, "未开发"),
    DEVELOP_AFOOT(2, "开发中"),
    DEVELOP_FINISH(3, "开发完成"),
    DISCONTINUE_DEVELOP(4, "中止开发"),
    SUSPEND_DEVELOP(5, "暂停开发");

    private Integer code;
    private String name;

    ProductDetailStateEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        ProductDetailStateEnum[] productDetailStateEnums = values();
        for (ProductDetailStateEnum productDetailStateEnum : productDetailStateEnums) {
            if (productDetailStateEnum.getCode() == code) {
                return productDetailStateEnum.getName();
            }
        }
        return null;
    }

    public static ProductDetailStateEnum getEnumByType(String code){
        ProductDetailStateEnum[] productDetailStateEnums = values();
        for (ProductDetailStateEnum productDetailStateEnum : productDetailStateEnums) {
            if (productDetailStateEnum.getCode().equals(code)) {
                return productDetailStateEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        ProductDetailStateEnum[] productDetailStateEnums = values();
        for (ProductDetailStateEnum productDetailStateEnum : productDetailStateEnums) {
            if (productDetailStateEnum.getName().equals(name)) {
                return productDetailStateEnum.getCode();
            }
        }
        return null;
    }
}
