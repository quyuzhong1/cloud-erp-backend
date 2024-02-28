package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 采购退货单异常类型
 */
public enum PoReturnUnusualTypeEnum implements EnumMessage {
    QUANTITY_ISSUE("quantityIssue", "数量问题"),
    OTHER_ISSUES("otherIssues", "其他问题"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    PoReturnUnusualTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public String getStatus() {
        return status;
    }
    @Override
    public String getCode() {
        return status;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (PoReturnUnusualTypeEnum item : PoReturnUnusualTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
