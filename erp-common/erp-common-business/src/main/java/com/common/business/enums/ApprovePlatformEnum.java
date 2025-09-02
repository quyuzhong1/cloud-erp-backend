package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 审核平台
 * @author will
 * @date 2025/8/5 09:57
 */
public enum ApprovePlatformEnum implements EnumMessage {

    FEI_SHU("feiShu", "飞书"),
    ERP("erp","ERP")
    ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;



    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    ApprovePlatformEnum(String code, String name){

        this.code = code;
        this.name = name;
    }

}
