package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @date 2024/2/26 17:49
 */
public enum ProductCertificateProjectEnum implements EnumMessage {


    CE("CE", "CE"),
    ROHS("RoHS", "RoHS"),
    FCC("FCC", "FCC"),
    FCC_ID("FCC ID", "FCC ID"),
    PSE("PSE", "PSE"),
    IC("IC", "IC"),
    KC("KC", "KC"),
    CCC("CCC", "CCC"),
    QC_REPORT("qcReport", "质检报告"),
    SRRC("SRRC", "SRRC"),
    REACH("Reach", "Reach"),
    TELEC("TELEC", "TELEC"),
    OTHER("other", "其他"),
    TRANSPORT_REPORT("transportReport", "运输报告"),
    OTHER_CERTIFICATE("otherCertificate", "其他认证"),
    ;

    private String code;
    private String name;

    ProductCertificateProjectEnum(String code, String name) {
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
        for (ProductCertificateProjectEnum item : ProductCertificateProjectEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        for (ProductCertificateProjectEnum item : ProductCertificateProjectEnum.values()) {
            if (item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return "";
    }
}
