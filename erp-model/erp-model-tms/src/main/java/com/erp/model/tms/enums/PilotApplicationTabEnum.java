package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 试产/量产单 tab页枚举
 * @date 2024-08-27
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PilotApplicationTabEnum implements EnumMessage {
    ALL("all", "全部"),
    WAIT_SUBMIT("waitSubmit", "待提交"),
    WAIT_ME_APPROVE("waitMeApprove", "待我审核"),
    REJECT("reject", "不通过"),
    NOT_ORDER("notOrder", "未下单"),
    ORDER("order", "已下单"),
    STOCK_IN("stockIn", "已入库");

    private String code;
    private String name;

    public static String getName(String code){
        for (PilotApplicationTabEnum tabEnum : values()) {
            if (tabEnum.getCode().equals(code)) {
                return tabEnum.getName();
            }
        }
        return "";
    }
}
