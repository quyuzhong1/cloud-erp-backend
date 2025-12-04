package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 仓库操作类型
 */
public enum WarehouseOperationTypeEnum implements EnumMessage {
    NO_OPEN_RELABLE("NO_OPEN_RELABLE", "不开箱换SKU标"),
    OPEN_RELABLE("OPEN_RELABLE", "开箱换SKU标"),
    PASTE_LABEL("PASTE_LABEL", "贴板标"),
    PASTE_PACKAGE("PASTE_PACKAGE", "贴箱唛"),
    OTHER("OTHER", "其他"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;



    WarehouseOperationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (WarehouseOperationTypeEnum billTypeEnum : WarehouseOperationTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }
}
