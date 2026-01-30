package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 上传记录 发票状态 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-03-07 14:15:46
 */
public enum InvoiceInfoStatusEnum implements EnumMessage {
	INVOICING("invoicing", "开票中"),
	INVOICE_FAILED("invoiceFailed", "开票失败"),
	INVOICE_SUCCESS("invoiceSuccess", "开票成功"),
	CANCELED("canceled", "已取消"),
	VOIDED("voided", "已作废"),
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

    InvoiceInfoStatusEnum(String code, String name) {
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
        for (InvoiceInfoStatusEnum statusEnum : InvoiceInfoStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
