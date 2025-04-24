package com.erp.model.sys.enums;

/**
 * 枚举
 * @author will
 * @date 2025/2/19 17:00
 */
public enum DictBasicEnum {

    FS_GROUP("fsGroup", "", "飞书群"),
    ;


    private String key;
    private String remark;
    private String desc;


    DictBasicEnum(String key, String remark, String desc) {
        this.key = key;
        this.remark = remark;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getRemark() {
        return remark;
    }

    public String getDesc() {
        return desc;
    }
}
