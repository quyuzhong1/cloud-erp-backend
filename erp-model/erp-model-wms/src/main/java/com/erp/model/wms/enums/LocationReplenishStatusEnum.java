package com.erp.model.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仓位补货：单据状态枚举
 * @date 2024-06-24
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum LocationReplenishStatusEnum {
    WAIT_HANDLE("wait_handle", "待处理"),
    HANDLE_ING("handle_ing", "处理中"),
    HANDLED("handled", "已处理"),
    NO_NEED_HANDLE("no_need_handle", "无需处理"),
    ;

    private String code;
    private String name;

    public static String getNameByCode(String code) {
        for (LocationReplenishStatusEnum e : LocationReplenishStatusEnum.values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }
}
