package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 发票状态
 */
public enum SoB2cNfeStatusEnum implements EnumMessage {
    PENDING("pending","待开票"),
    INVOICING("invoicing","开票中"),
    INVOICE_FAILURE("invoiceFailure","开票失败"),
    NOT_NEED_INVOICE("notNeedInvoice","无需开票"),
    WAIT_UPLOAD("waitUpload","待上传"),
    UPLOAD_FAILURE("uploadFailed","上传失败"),
    UPLOAD_SUCCESS("uploadSuccess","已上传"),
    NOT_NEED_UPLOAD("notNeedUpload","无需上传"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    SoB2cNfeStatusEnum(String code, String name) {
        this.code=code;
        this.name=name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cNfeStatusEnum statusEnum : SoB2cNfeStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
