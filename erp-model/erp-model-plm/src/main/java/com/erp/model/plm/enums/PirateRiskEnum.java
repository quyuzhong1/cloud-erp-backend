package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 8:55
 */
public enum PirateRiskEnum implements EnumMessage {

    RISK(1,"有风险"),
    NOTRISK(2,"无风险");


    private Integer code;

    private String name;


    PirateRiskEnum(Integer colourState, String name) {
        this.code = colourState;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (PirateRiskEnum pirateRiskEnum : PirateRiskEnum.values()) {
            if (pirateRiskEnum.getCode().equals(code)) {
                return pirateRiskEnum.getName();
            }
        }
        return "";
    }
}
