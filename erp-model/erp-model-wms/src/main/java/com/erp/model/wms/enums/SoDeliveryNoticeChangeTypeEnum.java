package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 发货通知单变更类型
 */
public enum SoDeliveryNoticeChangeTypeEnum implements EnumMessage {
    ADD("add", "新增" ),
    UPDATE("update", "修改"),
    DELETE("delete", "删除" ),
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

    SoDeliveryNoticeChangeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoDeliveryNoticeChangeTypeEnum billTypeEnum : SoDeliveryNoticeChangeTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }
}
