package com.erp.model.plm.enums;

/**
 * @Classname TaskProcessTypeEnum
 * @Description TODO
 * @Date 2022-10-20 15:02
 * @Created by yl
 */
public enum TaskProcessTypeEnum {

    GENERAL_TASK(0, "一般任务"),
    GENERAL_APPROVAL_TASK(1, "有文档审核任务"),
    REVIEW_TASK(2, "评审任务");

    private Integer code;
    private String name;

    TaskProcessTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
}
