package com.erp.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 11:30
 */
public enum SeasonEnum implements EnumMessage {

    SPRING("Q1", "春季","春季"),
    SUMMER("Q2", "夏季","夏季"),
    AUTUMN("Q3", "秋季","秋季"),
    WINTER("Q4", "冬季","冬季");


    private String code;

    private String name;

    private String desc;

    SeasonEnum(String code, String name,String desc) {
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

    public static SeasonEnum getNameByCode(String code) {
        SeasonEnum[] enums = values();
        for (SeasonEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }

    public static SeasonEnum getByName(String name) {
        SeasonEnum[] enums = values();
        for (SeasonEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum;
            }
        }
        return null;
    }
}
