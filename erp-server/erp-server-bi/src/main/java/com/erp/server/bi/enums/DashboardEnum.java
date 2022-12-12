package com.erp.server.bi.enums;

/**
 * @Classname  仪表盘相关枚举
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum DashboardEnum {

    PERSONAL("personal","私人"),
    SHARE("share","共享");



    private String flag;

    private String name;


    DashboardEnum(String flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }


}
