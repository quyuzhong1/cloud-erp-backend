package com.erp.model.wms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum
 * @Description TODO
 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    WAREHOUSE_TYPE("warehouseType", "warehouseType", "仓库类型");


    private String key;
    private String type;
    private String desc;


    DictBasicEnum(String key, String type, String desc) {
        this.key = key;
        this.type = type;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
