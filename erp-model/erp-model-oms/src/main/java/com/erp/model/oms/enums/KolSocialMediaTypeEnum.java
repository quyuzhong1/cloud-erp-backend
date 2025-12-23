package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 达人社媒数据类型枚举
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Getter
@AllArgsConstructor
public enum KolSocialMediaTypeEnum implements EnumMessage {

    /**
     * 手动
     */
    MANUAL("手动", "manual"),

    /**
     * 自动
     */
    AUTO("自动", "auto");

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 根据代码获取枚举
     */
    public static KolSocialMediaTypeEnum getByCode(String code) {
        for (KolSocialMediaTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     */
    public static KolSocialMediaTypeEnum getByName(String name) {
        for (KolSocialMediaTypeEnum type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String getCode() {
        return code;
    }
}

