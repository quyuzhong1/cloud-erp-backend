package com.erp.server.plm.enums;

/**
 * @Classname TaskStateEnum
 * @Description TODO
 * @Date 2022-09-19 16:43
 * @Created by yl
 */
public enum TaskStateEnum {

    TO_BE_RELEASED(0, "待发布"),
    NOT_START(1, "待开始"),
    WAIT_CONFIRM(2, "待审核"),
    ING(3, "进行中"),
    FINISH(4, "已完成"),
    CLOSE(5, "已取消"),
    APPROVAL_ING(7, "审核中"),
    APPROVAL_PASS(8, "审核通过"),
    APPROVAL_NO_PASS(9, "审核不通过");


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
