package com.erp.server.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 8:55
 */
public enum PirateRiskEnum implements EnumMessage {

    NOTRISK(0,"无风险"),
    RISK(1,"有风险");

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
            if (code.equals(pirateRiskEnum.getCode())) {
                return pirateRiskEnum.getName();
            }
        }
        return "";
    }
}
