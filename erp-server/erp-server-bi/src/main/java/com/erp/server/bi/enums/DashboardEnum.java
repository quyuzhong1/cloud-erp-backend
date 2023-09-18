package com.erp.server.bi.enums;

import lombok.Getter;

/**
 * @Classname  仪表盘相关枚举

 * @Date 2022-09-29 14:13
 * @Created by yl
 */
@Getter
public enum DashboardEnum {

    PERSONAL("personal","私人"),
    // 按用户ID共享
    SHARE("share","共享"),
    // 按多角色ID共享
    ROLE("role","角色"),
    ;



    private final String flag;

    private final String name;


    DashboardEnum(String flag, String name) {
        this.flag = flag;
        this.name = name;
    }

}
