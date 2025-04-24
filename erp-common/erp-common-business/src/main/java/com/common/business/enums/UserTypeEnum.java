package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author zdy
 * @Classname SkuTypeEnum
 * @Date 2023-09-19 9:25
 * @Created by yl
 */
public enum UserTypeEnum implements EnumMessage {
    ERP("erp","ERP系统"),
    SRM("srm","SRM系统"),
    PDA("pda","PDA系统"),
    ;

    UserTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public final String code;
    /**
     * 名称
     */
    private final String name;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
