package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 补货建议来源单据状态
 */
@Getter
@AllArgsConstructor
public enum ReplenishmentBillStatusEnum implements EnumMessage {
    DRAFT("draft", "草稿"),
    WAIT_CONFIRM("waitConfirm", "待确认"),
    FINISH("finish", "已完成&未下推"),
    WAIT_SUBMIT("waitSubmit", "待提交"),
    APPROVE_ING("approveIng", "待审核"),
    APPROVE("approve", "已审核&未发货"),
    TO_BE_CREATE("toBeCreate", "待生成"),
    CREATED("created", "已生成"),
    ;


    private final String code;

    private final String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
