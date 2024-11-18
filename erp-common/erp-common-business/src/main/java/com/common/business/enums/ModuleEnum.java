package com.common.business.enums;

/**
 * @Classname ModuleEnum

 * @Date 2023-02-09 10:40
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (ModuleEnum item : ModuleEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
