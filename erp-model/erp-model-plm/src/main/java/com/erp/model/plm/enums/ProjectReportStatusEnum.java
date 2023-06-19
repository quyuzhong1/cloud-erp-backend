package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum  ProjectReportStatusEnum  implements EnumMessage {
    NOTAPPROVAL("notApproval","未立项"),
    APPROVAL("approval","已立项"),
    FINISHED("finished","已完成"),

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



    ProjectReportStatusEnum(String code, String name) {
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
        for (ProjectReportStatusEnum billTypeEnum : ProjectReportStatusEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

}
