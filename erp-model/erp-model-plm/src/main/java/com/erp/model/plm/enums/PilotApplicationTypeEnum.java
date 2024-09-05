package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 试产/量产单类型
 * @date 2024-08-27
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PilotApplicationTypeEnum implements EnumMessage {
    TRIAL("trial", "试产"),
    BATCH("batch", "量产");

    private String code;
    private String name;

    public static String getName(String code){
        for (PilotApplicationTypeEnum typeEnum : values()) {
            if(typeEnum.getCode().equals(code)){
                return typeEnum.getName();
            }
        }
        return "";
    }
}
