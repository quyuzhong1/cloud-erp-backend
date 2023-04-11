package com.common.message.enums;

/**
 * @author Will
 * @version 1.0
 * @description: API模块类型枚举
 * @date 2023/1/11 14:54
 */
public enum ApiModuleTypeEnum {

    PRODUCTDETAIL(0, "productDetail", "产品信息"),
    BOMMANAGE(1, "bomManage", "BOM管理"),

    STOCK_OVERSEAS(2, "stockOverseas", "海外仓库存"),

    CHANGE_ORG(7, "changeOrg", "默认组织切换"),
    ;


    private Integer code;

    private String name;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    ApiModuleTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ApiModuleTypeEnum getByCode(Integer code) {
        ApiModuleTypeEnum[] values = values();
        for (ApiModuleTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ApiModuleTypeEnum getByName(String name) {
        ApiModuleTypeEnum[] values = values();
        for (ApiModuleTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
