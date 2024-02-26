package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @date 2024/2/26 17:49
 */
public enum ProductCertificateTypeEnum implements EnumMessage {


    NO_DEVELOP("", "未开发");

    private String code;
    private String name;

    ProductCertificateTypeEnum(String code, String name) {
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


}
