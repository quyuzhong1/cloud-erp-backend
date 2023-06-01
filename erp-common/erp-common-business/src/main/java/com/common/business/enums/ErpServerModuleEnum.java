package com.common.business.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname: ErpServerModuleEnum
 * @Description: ERP系统服务模块
 * @CreateTime: 2023-06-01  09:44
 * @Author: zhangchunlin
 */
public enum ErpServerModuleEnum {

    ERP_SERVER_ADMIN("erp-server-admin","admin管理系统"),
    ERP_SERVER_AUTH("erp-server-auth","权限系统"),
    ERP_SERVER_SYS("erp-server-sys","系统服务"),
    ERP_SERVER_BI("erp-server-bi","BI系统"),
    ERP_SERVER_DMP("erp-server-bi","数据管理系统"),
    ERP_SERVER_MSG("erp-server-msg","消息中心系统"),
    ERP_SERVER_OMS("erp-server-oms","订单管理系统"),
    ERP_SERVER_PLM("erp-server-plm","产品计划系统"),
    ERP_SERVER_SCM("erp-server-scm","供应链系统"),
    ERP_SERVER_WMS("erp-server-wms","仓储管理系统"),
    ERP_SERVER_WORKFLOW("erp-server-workflow","工作流系统"),
    ;
    public String code;
    public String name;

    public String code() {
        return code;
    }

    ErpServerModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static ErpServerModuleEnum of(String code) {
        return Arrays.stream(ErpServerModuleEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 通过代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        ErpServerModuleEnum erpServerModuleEnum = of(code);
        return Optional.ofNullable(erpServerModuleEnum).map(ErpServerModuleEnum::getName).orElse("");
    }

}
