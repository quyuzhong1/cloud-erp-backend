package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @description: 来源系统枚举
 * @date 2023/10/12 11:47
 */
public enum SourcePlatformEnum {

    ERP_BI("erp-bi", "bi服务"),
    ERP_DMP("erp-dmp", "dmp服务"),
    ERP_OMS("erp-oms", "oms服务"),
    ERP_PLM("erp-plm", "plm服务"),
    ERP_SYS("erp-sys", "sys服务"),
    ERP_SCM("erp-scm", "scm服务"),
    ERP_WMS("erp-wms", "wms服务"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    SourcePlatformEnum(String type, String name) {
        this.code = type;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
