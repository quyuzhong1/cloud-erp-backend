package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;


public enum ServiceCodeNameEnum implements EnumMessage {
	DEFAULT("default","默认本系统"),
	DMP("dmp","中台系统"),
	OMS("oms","订单系统"),
	PLM("plm","产品系统"),
	SCM("scm","供应链系统"),
	SRM("srm","供应商系统"),
	SYS("sys","系统"),
    TMS("tms","物流系统"),
    WMS("wms","仓储系统"),
    ;

    @EnumValue
    @JsonValue
    private String code;

    private String name;
    ServiceCodeNameEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
