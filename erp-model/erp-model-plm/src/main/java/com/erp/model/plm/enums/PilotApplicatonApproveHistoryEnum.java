package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 试产量产单 审核记录枚举
 * @date 2024-09-06
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PilotApplicatonApproveHistoryEnum implements EnumMessage {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    APPROVE_ING("approveIng", "审核中"),
    REJECT("reject", "审核不通过"),
    APPROVE("approve", "审核通过");

    private String code;
    private String name;

    public static String getName(String code){
        for (PilotApplicatonApproveHistoryEnum value : values()) {
            if (value.getCode().equals(code)){
                return value.getName();
            }
        }
        return "";
    }
}
