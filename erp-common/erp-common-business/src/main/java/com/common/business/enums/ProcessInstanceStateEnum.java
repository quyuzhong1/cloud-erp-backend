package com.common.business.enums;

/**
 * @Classname ProcessInstanceStateEnum1
 * @Description TODO
 * @Date 2022-08-16 15:47
 * @Created by yl
 */
public enum ProcessInstanceStateEnum {

    PROCESS_ING(0,"流程进行中"),
    PROCESS_ENDED(5,"流程已结束"),
    PROCESS_NON_EXISTENT(6, "流程不存在");


    private Integer code;
    private String name;

    ProcessInstanceStateEnum(Integer code, String name) {
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



