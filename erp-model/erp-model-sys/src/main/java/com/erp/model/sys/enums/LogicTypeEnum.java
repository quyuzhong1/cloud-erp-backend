package com.erp.model.sys.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 逻辑类型枚举
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Getter
@AllArgsConstructor
public enum LogicTypeEnum {

    /**
     * 旧逻辑
     */
    OLD("OLD", "旧逻辑"),

    /**
     * 优化的新逻辑
     */
    NEW("NEW", "优化的新逻辑");

    /**
     * 逻辑类型代码
     */
    private final String code;

    /**
     * 逻辑类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 逻辑类型代码
     * @return 逻辑类型枚举
     */
    public static LogicTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LogicTypeEnum logicType : values()) {
            if (logicType.getCode().equals(code)) {
                return logicType;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     *
     * @param code 逻辑类型代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
