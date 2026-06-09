package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * B2B三方发货单装箱类型（与谷仓 packing_type 一致）
 */
public enum B2bPackingTypeEnum implements EnumMessage {

    WAREHOUSE_SELF("0", "仓库自主装箱"),
    CUSTOMER_SPECIFIED("1", "客户指定装箱"),
    PRE_STAGED_BOX("2", "已暂存箱发货"),
    ;

    private final String code;
    private final String name;

    B2bPackingTypeEnum(String code, String name) {
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
        for (B2bPackingTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "";
    }

    public static boolean requiresPackingDetail(String code) {
        return CUSTOMER_SPECIFIED.code.equals(code) || PRE_STAGED_BOX.code.equals(code);
    }
}
