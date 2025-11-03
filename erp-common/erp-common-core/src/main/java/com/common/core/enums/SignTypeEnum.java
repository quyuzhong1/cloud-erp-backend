package com.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 对称加密算法枚举
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Getter
@AllArgsConstructor
public enum SignTypeEnum {

    /**
     * AES加密算法
     */
    AES("AES", "AES加密算法"),

    /**
     * HMAC加密算法
     */
    HMAC("HMAC", "HMAC加密算法");

    /**
     * 算法代码
     */
    private final String code;

    /**
     * 算法描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 算法代码
     * @return 加密算法枚举
     */
    public static SignTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SignTypeEnum algorithm : values()) {
            if (algorithm.getCode().equals(code)) {
                return algorithm;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     *
     * @param code 算法代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
