package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 发送状态
 * @date 2023/1/12 15:33
 */
public enum ApiKingdeeOrganizationEnum {

    ORGANIZATION_WEIJI("1", "100","唯迹集团", "唯迹集团"),
    ORGANIZATION_YZS("100504", "101","优至胜", "优至胜"),
    ORGANIZATION_XX("173616", "105","小隼", "小隼"),
    ;
    private String code;

    private String number;

    private String name;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    ApiKingdeeOrganizationEnum(String code, String number, String name, String desc) {
        this.code = code;
        this.number = number;
        this.name = name;
        this.desc = desc;
    }

    public static ApiKingdeeOrganizationEnum getByCode(String code) {
        ApiKingdeeOrganizationEnum[] values = values();
        for (ApiKingdeeOrganizationEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ApiKingdeeOrganizationEnum getByName(String name) {
        ApiKingdeeOrganizationEnum[] values = values();
        for (ApiKingdeeOrganizationEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public String getNumber() {
        return number;
    }
}
