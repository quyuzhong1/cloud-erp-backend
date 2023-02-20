package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: 任务优先级枚举
 * @date 2023/1/5 16:46
 */
public enum TaskPriorityEnum implements EnumMessage {

    LOWER(1, "低级"),
    INTERMEDIATE(2, "中级"),
    SENIOR(3, "高级");

    private Integer code;
    private String name;

    TaskPriorityEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (TaskPriorityEnum state : TaskPriorityEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

}
