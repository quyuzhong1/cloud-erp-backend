package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 发送状态
 * @date 2023/1/12 15:33
 */
public enum ApiStatusEnum {

    FAILURE(0, "failure", "发送失败"),
    SUCCESS(1, "success", "发送成功");

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

    ApiStatusEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ApiStatusEnum getByCode(Integer code) {
        ApiStatusEnum[] values = values();
        for (ApiStatusEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ApiStatusEnum getByName(String name) {
        ApiStatusEnum[] values = values();
        for (ApiStatusEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
