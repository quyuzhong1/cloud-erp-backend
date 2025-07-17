package com.common.business.enums;

/**
 * 启禁用枚举
 * @author will
 * @date 2025/5/15 16:13
 */
public enum DisabledEnum {
    All(null, "全部"),
    DISABLED(true, "禁用"),
    ENABLE(false, "启用");

    private Boolean code;
    private String name;

    DisabledEnum(Boolean code, String name) {
        this.code = code;
        this.name = name;
    }

    public Boolean getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Boolean code) {
        for (DisabledEnum item : DisabledEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
