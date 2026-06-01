package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * B2B三方发货单装箱标签尺寸
 */
public enum B2bPackingLabelSizeEnum implements EnumMessage {

    SIZE_100_150("100*150", "100*150"),
    ;

    private final String code;
    private final String name;

    B2bPackingLabelSizeEnum(String code, String name) {
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

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return true;
        }
        for (B2bPackingLabelSizeEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
