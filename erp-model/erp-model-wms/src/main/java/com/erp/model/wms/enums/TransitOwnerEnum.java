package com.erp.model.wms.enums;

/**
 * @Classname: TransitOwnerEnum
 * @Description: 在途归属
 * @CreateTime: 2023-05-26  09:07
 * @Author: zhangchunlin
 */
public enum TransitOwnerEnum {

    TRANSFER_IN("in", "调入方"),
    TRANSFER_OUT("out", "调出方"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    TransitOwnerEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
