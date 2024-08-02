package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: b2c销售订单导出类型
 * @author Will
 * @date: 2024/4/29 15:38
 */
public enum SoB2cExportTypeEnum implements EnumMessage {
    PARENT_EXPORT("parentExport","销售套装BOM按父件SKU导出"),
    CHILD_EXPORT("childExport","销售套装BOM按子件SKU导出"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    SoB2cExportTypeEnum(String code, String name) {
        this.code=code;
        this.name=name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cExportTypeEnum statusEnum : SoB2cExportTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
