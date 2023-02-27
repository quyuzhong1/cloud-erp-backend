package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 10:31
 */
public enum ProductStyleEnum implements EnumMessage {

    STYLE_CONVENTION("convention", "常规款",""),
    STYLE_PROFIT("profit", "利润款",""),
    STYLE_RATE_FLOW("rate_flow", "流量款",""),
    STYLE_HOT("hot", "爆款",""),
    STYLE_KING("king", "王炸款",""),
    STYLE_CONCEPT("concept", "概念款",""),
    STYLE_PUBLIC_RELATION("public_relation", "公关款","");


    private String code;

    private String name;

    private String desc;

    ProductStyleEnum(String code, String name,String desc) {
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

    public static String getNameByCode(String code) {
        ProductStyleEnum[] enums = values();
        for (ProductStyleEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum.getName();
            }
        }
        return "";
    }
    public static String getByName(String name) {
        ProductStyleEnum[] enums = values();
        for (ProductStyleEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        ProductStyleEnum[] enums = values();
        for (ProductStyleEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum.getCode();
            }
        }
        return "";
    }
}
