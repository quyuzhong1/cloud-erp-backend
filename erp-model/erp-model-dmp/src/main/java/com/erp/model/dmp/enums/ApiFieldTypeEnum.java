package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: API字段取值方式枚举
 * @date 2023/1/11 14:58
 */
public enum ApiFieldTypeEnum {

    FIELD_VALUE_COPY(0, "field_value_copy", "直接复制"),
    FIELD_VALUE_MAP(1, "field_value_map", "按对照表赋值");

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

    ApiFieldTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ApiFieldTypeEnum getByCode(Integer code) {
        ApiFieldTypeEnum[] values = values();
        for (ApiFieldTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ApiFieldTypeEnum getByName(String name) {
        ApiFieldTypeEnum[] values = values();
        for (ApiFieldTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
