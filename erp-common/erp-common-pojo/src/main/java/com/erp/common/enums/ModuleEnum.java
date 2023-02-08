package com.erp.common.enums;

/**
 * @Classname ModuleEnum
 * @Description TODO
 * @Date 2023-02-08 15:45
 * @Created by yl
 */
public enum ModuleEnum {
    PLM_SCHEDULE_TASK("plmScheduleTask","plm项目计划任务");
    public String code;
    public String name;

    public String code() {
        return code;
    }
    ModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
