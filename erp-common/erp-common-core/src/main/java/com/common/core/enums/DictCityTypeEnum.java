package com.common.core.enums;

import com.common.core.constant.EnumMessage;

/**
 * 城市表类型枚举
 * @author will
 * @date 2025/7/24 18:56
 */
public enum DictCityTypeEnum implements EnumMessage {



    PROVINCE("province", "省份"),
    CITY("city", "城市"),
    DISTRICT("district", "街道"),
    ;


    private String code;

    private String name;


    DictCityTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (DictCityTypeEnum item : DictCityTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
