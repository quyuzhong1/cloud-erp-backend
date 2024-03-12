package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @date 2024/2/26 17:49
 */
public enum ProductCertificateTypeEnum implements EnumMessage {


    PRODUCT_ATTESTATION("productAttestation", "产品认证"),
    OTHER_ATTESTATION("otherAttestation", "其他认证"),
    TRANSPORT_ATTESTATION("transportAttestation", "运输认证"),
    ;

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

    public static String getName(String code) {
        for (ProductCertificateTypeEnum item : ProductCertificateTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        for (ProductCertificateTypeEnum item : ProductCertificateTypeEnum.values()) {
            if (item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return "";
    }
}
