package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 发票状态
 */
public enum SoB2cVatStatusEnum implements EnumMessage {
    PENDING("pending","待开票"),
    WAIT_UPLOAD("waitUpload","已开票"),
    INVOICE_FAILED("invoiceFailed","开票失败"),
    NOT_NEED_INVOICE("notNeedInvoice","无需开票"),
    UPLOAD_FAILURE("uploadFailure","上传失败"),
    UPLOADING("uploading","上传中"),
    UPLOAD_SUCCESS("uploadSuccess","已上传"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    SoB2cVatStatusEnum(String code, String name) {
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
        for (SoB2cVatStatusEnum statusEnum : SoB2cVatStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
