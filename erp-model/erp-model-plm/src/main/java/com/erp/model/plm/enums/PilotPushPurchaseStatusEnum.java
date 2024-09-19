package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 试产量产单下推采购申请单状态
 * @date 2024-09-06
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PilotPushPurchaseStatusEnum implements EnumMessage {

    NOT_ORDER("notOrder", "未下单"),
    PART_ORDER("partOrder", "部分下单"),
    ORDER("order", "已下单");

    private String code;
    private String name;

    public static String getName(String code){
        for (PilotPushPurchaseStatusEnum statusEnum : values()) {
            if(statusEnum.getCode().equals(code)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
