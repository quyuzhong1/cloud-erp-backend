package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 发送状态
 * @date 2023/1/12 15:33
 */
public enum ApiKingdeeOrganizationEnum {

    ORGANIZATION_WEIJI(0, "唯迹集团", "唯迹集团");

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

    ApiKingdeeOrganizationEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ApiKingdeeOrganizationEnum getByCode(Integer code) {
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
}
