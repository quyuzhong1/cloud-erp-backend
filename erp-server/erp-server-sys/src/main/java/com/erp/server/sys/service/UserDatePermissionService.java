package com.erp.server.sys.service;

public interface UserDatePermissionService {
    /**
     * 查询用户数据权限,获取到权限sql
     * @Author Luo_WG
     * @Date 2024/3/12 16:45
     * @param tableField 权限过滤字段
     * @param menuCode 菜单编号
     * @return java.lang.String
     **/
    String getUserDatePermissionSql(String tableField, String menuCode);
    /**
     * 根据菜单cdoe查询用户数据权限
     * @author hyj
     * @date 2024/5/9 10:43
     * @param menuCode 菜单编号
     * @return java.lang.String
     **/
    Boolean getUserDatePermissionByMenuCode(String menuCode);
}
