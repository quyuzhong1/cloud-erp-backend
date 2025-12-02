package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回片状态枚举
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatusEnum  implements EnumMessage {

    /**
     * 待回片
     */
    PENDING("待回片", "pending"),

    /**
     * 已回片
     */
    COMPLETED("已回片", "completed");

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 根据代码获取枚举
     */
    public static FeedbackStatusEnum getByCode(String code) {
        for (FeedbackStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     */
    public static FeedbackStatusEnum getByName(String name) {
        for (FeedbackStatusEnum status : values()) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        return null;
    }
    @Override
    public String getCode() {
        return code;
    }
}

