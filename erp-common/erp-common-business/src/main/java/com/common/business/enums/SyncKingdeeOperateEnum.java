package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/11 17:13
 */
public enum SyncKingdeeOperateEnum {

    OPERATE_ADD("operateAdd", "新增"),
    OPERATE_UPDATE("operateUpdate", "修改"),
    OPERATE_ENABLE("operateEnable", "启用"),
    OPERATE_DISABLE("operateDisable", "禁用"),
    OPERATE_DELETE("operateDelete", "删除"),
    ;
    private String code;

    private String name;


    SyncKingdeeOperateEnum(String code, String name) {
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
