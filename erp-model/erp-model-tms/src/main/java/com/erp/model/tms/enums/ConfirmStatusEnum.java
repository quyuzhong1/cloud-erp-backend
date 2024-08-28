package com.erp.model.tms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @date 2024-08-16
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum ConfirmStatusEnum {

    TO_BE_CONFIRM("toBeConfirm", "待确认"),
    CONFIRMED("confirmed", "已确认")
    ;

    private String code;
    private String name;

    public static ConfirmStatusEnum getByCode(String code) {
        for (ConfirmStatusEnum e : ConfirmStatusEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        for (ConfirmStatusEnum e : ConfirmStatusEnum.values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }
}
