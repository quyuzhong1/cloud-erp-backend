package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @date 2024/2/26 17:49
 */
public enum ProductCertificateProjectEnum implements EnumMessage {


    CE("CE", "CE"),
    RoHS("RoHS", "RoHS"),
    FCC("FCC", "FCC"),
    FCC_ID("FCC ID", "FCC ID"),
    PSE("PSE", "PSE"),
    IC("IC", "IC"),
    KC("KC", "KC"),
    TELEC("TELEC", "TELEC"),
    OTHER("other", "其他"),
    TRANSPORT_REPORT("transportReport", "运输报告"),

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


}
