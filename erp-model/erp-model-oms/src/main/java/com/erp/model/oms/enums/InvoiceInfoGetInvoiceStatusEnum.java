package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 开票清单「获取发票」状态枚举
 *
 * @author cursor
 */
public enum InvoiceInfoGetInvoiceStatusEnum implements EnumMessage {
    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    InvoiceInfoGetInvoiceStatusEnum(String code, String name) {
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

    /**
     * 按 code 取展示名；空或未知返回空串
     *
     * @param code 状态码
     * @return 名称
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (InvoiceInfoGetInvoiceStatusEnum statusEnum : InvoiceInfoGetInvoiceStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
