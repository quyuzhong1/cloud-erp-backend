package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 中转报关状态
 */
public enum TransferLogisticsStatusEnum implements EnumMessage {

    DELETED("deleted", "删除"),
    DRAFT("draft", "草稿"),
    UNUSUAL("unusual", "异常"),
    CONFIRMED("confirmed", "已确认"),
    SUBMITTED("submitted", "已提交"),
    OUTSTOCK("outstock", "已出货"),
    SIGNED("signed", "已签收"),

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

    TransferLogisticsStatusEnum(String code, String name) {
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
        for (TransferLogisticsStatusEnum statusEnum : TransferLogisticsStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
