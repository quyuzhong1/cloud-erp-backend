package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum NoticeTemplateEnum implements EnumMessage {
    APPROVE("approve","1008" ),
    APPROVE_RESULT_PASS("approveResult","1004" ),
    APPROVE_RESULT_REJECT("approveResult","1003" ),
    RECALL("recall","1015" ),
    CC("cc","1016"),
    TIMEOUTWARNING("timeoutWarning","1021"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    NoticeTemplateEnum(String code, String name){
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgApproveNoticeNoticeTypeEnum statusEnum : CfgApproveNoticeNoticeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}