package com.erp.sdk.oms.yunting.cem.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 翻译字段类型枚举
 *
 * @author ERP System
 */
@Getter
@AllArgsConstructor
public enum TranslateFieldEnum {

    /**
     * 内容
     */
    CONTENT("CONTENT", "内容"),

    /**
     * 标题
     */
    COMMENT_TITLE("COMMENT_TITLE", "标题");

    /**
     * 字段代码
     */
    private final String code;

    /**
     * 字段描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static TranslateFieldEnum getByCode(String code) {
        for (TranslateFieldEnum field : values()) {
            if (field.getCode().equals(code)) {
                return field;
            }
        }
        return null;
    }
}

