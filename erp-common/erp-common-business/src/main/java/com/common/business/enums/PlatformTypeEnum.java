package com.common.business.enums;

import org.apache.commons.lang3.StringUtils;

/**
 */
public enum PlatformTypeEnum {


    SALES("sales", "销售平台"),
    LOGISTICS("logistics", "物流平台"),
    ;
    private String code;
    private String name;

    PlatformTypeEnum(String status, String name) {
        this.code = status;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (PlatformTypeEnum item : PlatformTypeEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
