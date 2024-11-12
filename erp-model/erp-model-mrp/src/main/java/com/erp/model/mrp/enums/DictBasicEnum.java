package com.erp.model.mrp.enums;

/**
 * @description: 字典类型枚举
 * @author Will
 * @date: 2023/11/8 11:02
 */
public enum DictBasicEnum {


    ;
    private String type;
    private String desc;


    DictBasicEnum(String type, String desc) {

        this.type = type;
        this.desc = desc;
    }


    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
