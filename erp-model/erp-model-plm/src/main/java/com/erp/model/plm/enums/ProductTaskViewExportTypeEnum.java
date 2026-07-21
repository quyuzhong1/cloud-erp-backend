package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * 项目任务视图导出类型，与 {@link com.erp.model.plm.dto.ProductTaskViewSearchDTO#getType()} 一致。
 */
@Getter
public enum ProductTaskViewExportTypeEnum implements EnumMessage {

    PERSONNEL(1, "按人员查看"),
    PRODUCT(2, "按产品查看"),
    PHASE(3, "按阶段查看"),
    IN_WAREHOUSE_TIME(4, "按量产入库时间查看");

    private final Integer code;
    private final String name;

    ProductTaskViewExportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ProductTaskViewExportTypeEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductTaskViewExportTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
