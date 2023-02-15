package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/3 11:17
 */
public enum TaskSearchCategoryEnum {

    TOMEPRODUCTTASKLIST(1, "toMeProductTaskList"),
    TOMEPLANENDTIMETASKLIST(2, "toMePlanEndTimeTaskList"),
    MYCREATEPRODUCTTASKLIST(3, "myCreateProductTaskList"),
    MYCREATEPLANENDTIMETASKLIST(4, "myCreatePlanEndTimeTaskList"),
    ALLPRODUCTTASKLIST(5, "allProductTaskList"),
    ALLPLANTIMETASKLIST(6, "allPlanTimeTaskList"),
    TOMEWAITAUDITPRODUCTTASKLIST(7, "toMeWaitAuditProductTaskList"),
    TOMEWAITAUDITPLANENDTIMETASKLIST(8, "toMeWaitAuditPlanEndTimeTaskList");

    private Integer code;
    private String name;

    TaskSearchCategoryEnum(Integer code, String name) {
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
