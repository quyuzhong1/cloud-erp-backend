package com.erp.server.sys.constant;

/**
 * @Classname 系统常量

 * @Date 2022-07-18 11:18
 * @Created by yl
 */
public class SysConstant {

    public static final Integer DEPARTMENT_TYPE = 1;

    public static final Integer GROUP_TYPE = 2;

    //目录
    public static final Integer CATALOG_TYPE = 1;

    public static final Integer MENU_TYPE = 2;

    public static final Integer BUTTON_TYPE = 3;

    public static final Integer FUNCTION_TYPE = 4;

    /**
     * 字段权限码菜单：menu_code 同时作为 cfg_mask_field.permission_code，
     * 挂到角色 sys_role_menu 即放开该字段的明文查看权限。
     */
    public static final Integer FIELD_PERMISSION_TYPE = 5;


    public static final Integer YES_STATE = 1;

    public static final Integer NO_STATE = 0;

    //超级管理员账户
    public static final String ADMIN_USER = "admin";

    public static final String ADMIN_PERMISSON_SQL = "";
}
