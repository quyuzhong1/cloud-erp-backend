package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * VAT发票状态（pending待开票，waitUpload已开票，invoiceFailed开票失败，uploadFailed上传失败，uploadSuccess已上传）
 * </p>
 *
 * @author zdy
 * @since 2025-03-07 14:15:46
 */
public enum VatInvoiceStatusEnum implements EnumMessage {
	PENDING("pending", "待开票"),
	WAIT_UPLOAD("waitUpload", "已开票"),
	INVOICE_FAILED("invoiceFailed", "开票失败"),
	UPLOAD_FAILED("uploadFailed", "上传失败"),
    UPLOAD_SUCCESS("uploadSuccess", "已上传"),
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

    VatInvoiceStatusEnum(String code, String name) {
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
        for (VatInvoiceStatusEnum statusEnum : VatInvoiceStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
