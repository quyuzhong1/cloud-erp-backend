package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购退货原因类型
 */
@Getter
@AllArgsConstructor
public enum PoReturnReasonTypeEnum implements EnumMessage {

    DEFECT("defect", "瑕疵品"),
    OTHER("other", "其它");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static String getName(String code) {
        for (PoReturnReasonTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
