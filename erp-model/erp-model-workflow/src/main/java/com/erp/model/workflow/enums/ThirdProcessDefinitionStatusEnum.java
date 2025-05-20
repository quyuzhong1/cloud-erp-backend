package com.erp.model.workflow.enums;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/14 17:32
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-14
 *@Description:
 *@Version: 1.0
 */
public enum ThirdProcessDefinitionStatusEnum {
    //plm
    ACTIVE("active", "已启用"),
    INACTIVE("inactive", "已停用"),
    DELETED("delete", "已删除"),
    UNKNOWN("unknown", "未知"),

    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ThirdProcessDefinitionStatusEnum(String code, String name) {
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
        for (TableNameEnum state : TableNameEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static TableNameEnum getByCode(String code) {
        return Arrays.stream(TableNameEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
