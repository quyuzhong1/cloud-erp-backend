package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ApproveStatusWithoutRejectEnum implements EnumMessage  {
    WAIT_SUBMIT("waitSubmit", "待提交", "待提交"),
    APPROVE_ING("approveIng", "审核中","待审核"),
    APPROVE("approve", "已审核","已审核");

    @EnumValue
    @JsonValue
    private String status;
    private String name;
    private String tableName;


    @Override
    public String getCode() {
        return status;
    }
}
