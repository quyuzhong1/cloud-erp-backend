package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 10:43
 */
public enum ProductTypeEnum implements EnumMessage {

    NEW_PRODUCTS("old", "新品","新品"),
    OLD_PRODUCTS("new", "老品","老品");


    private String code;

    private String name;

    private String desc;

    ProductTypeEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static ProductTypeEnum getNameByCode(String code) {
        ProductTypeEnum[] enums = values();
        for (ProductTypeEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }
    public static ProductTypeEnum getByName(String name) {
        ProductTypeEnum[] enums = values();
        for (ProductTypeEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum;
            }
        }
        return null;
    }
}
