package com.erp.model.plm.enums;

import java.util.Objects;

/**
 * 产品违禁词检测状态
 */
public enum ProductForbiddenWordCheckStatusEnum {

    WAIT(0, "待检测"),
    RUNNING(1, "检测中"),
    FINISH(2, "已检测"),
    FAIL(3, "检测失败");

    private final Integer code;
    private final String name;

    ProductForbiddenWordCheckStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ProductForbiddenWordCheckStatusEnum item : values()) {
            if (Objects.equals(item.getCode(), code)) {
                return item.getName();
            }
        }
        return "";
    }
}
