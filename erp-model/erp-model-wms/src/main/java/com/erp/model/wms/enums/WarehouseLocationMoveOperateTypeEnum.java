package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum WarehouseLocationMoveOperateTypeEnum implements EnumMessage {

    AFTER_SALES_SHELVING("afterSalesShelving", "售后上架"),
    FULL_BOX_TRANSFER("fullBoxTransfer", "整箱移位"),
    UNBOX_TRANSFER("unboxTransfer", "拆箱移位"),
    PICKING_TRANSFER("pickingTransfer", "拣货移位"),
    SELF_BUILT_TRANSFER("selfBuiltTransfer", "自建移位");

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;

    WarehouseLocationMoveOperateTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        String defaultCode = defaultCode(code);
        for (WarehouseLocationMoveOperateTypeEnum typeEnum : WarehouseLocationMoveOperateTypeEnum.values()) {
            if (typeEnum.getCode().equals(defaultCode)) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static String defaultCode(String code) {
        if (StringUtils.isBlank(code)) {
            return SELF_BUILT_TRANSFER.getCode();
        }
        return code;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
