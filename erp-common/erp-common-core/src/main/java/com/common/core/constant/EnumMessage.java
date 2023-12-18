package com.common.core.constant;

import java.util.Arrays;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/7 10:11
 */
public interface EnumMessage {

    Object getCode();
    String getName();

    // 默认方法，通过code获取name
    static String getNameByCode(Class<? extends EnumMessage> enumType, String code) {
        for (EnumMessage enumValue : enumType.getEnumConstants()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue.getName();
            }
        }
        return null;
    }
}
