package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @Classname  报关信息状态 not:待维护 completed:已维护
 * @Date 2025-07-16
 * @Created jack
 */
public enum LogisticsProductCustomsStatusEnum implements EnumMessage {

    NOT("not","待维护"),
    COMPLETED("completed","已维护"),
    ;

    private String code;

    private String name;


    LogisticsProductCustomsStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }



    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (LogisticsProductCustomsStatusEnum state : LogisticsProductCustomsStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static LogisticsProductCustomsStatusEnum getEnum(String code) {
        for (LogisticsProductCustomsStatusEnum state : LogisticsProductCustomsStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }
}
