package com.erp.model.mrp.enums;

import lombok.AllArgsConstructor;

/**
 * @description: 字典类型枚举
 * @author Will
 * @date: 2023/11/8 11:02
 */
@AllArgsConstructor
public enum DictBasicEnum {


    ;
    private String type;
    private String desc;



    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
