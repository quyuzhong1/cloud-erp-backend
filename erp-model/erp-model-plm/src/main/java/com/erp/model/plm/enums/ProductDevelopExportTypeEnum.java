package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * 产品开发导出数据类型
 *
 * @author jack
 */
@Getter
public enum ProductDevelopExportTypeEnum implements EnumMessage {

    PRODUCT(0, "产品列表"),
    TASK(1, "任务列表");

    private final Integer code;

    private final String name;

    ProductDevelopExportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isValid(Integer code) {
        return getEnum(code) != null;
    }

    public static ProductDevelopExportTypeEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductDevelopExportTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
