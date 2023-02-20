package com.erp.model.plm.enums;

/**   任务类型
 * @Classname
 * @Description TODO
 * @Date 2022-09-19 16:43
 * @Created by yl
 */
public enum TaskTypeEnum {

    GENERAL_TASK(0, "一般任务"),
    REVIEW_TASK(1, "审核任务");

    private Integer code;
    private String name;

    TaskTypeEnum(Integer code, String name) {
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
        for (TaskTypeEnum state : TaskTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
