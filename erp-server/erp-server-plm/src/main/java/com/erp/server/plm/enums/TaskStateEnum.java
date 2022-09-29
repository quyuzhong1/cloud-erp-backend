package com.erp.server.plm.enums;

/**
 * @Classname TaskStateEnum
 * @Description TODO
 * @Date 2022-09-19 16:43
 * @Created by yl
 */
public enum TaskStateEnum {

    TO_BE_RELEASED(0, "待发布"),
    NOT_START(1, "未开始"),
    ING(2, "进行中"),
    FINISH(3, "已完成"),
    FINISH_WAIT_CONFIRM(4, "完成待确认"),
    APPROVAL_ING(5, "审核中"),
    APPROVAL_PASS(6, "审核通过"),
    APPROVAL_NO_PASS(7, "审核不通过");

    private Integer code;
    private String name;

    TaskStateEnum(Integer code, String name) {
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
        for (TaskStateEnum state : TaskStateEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
