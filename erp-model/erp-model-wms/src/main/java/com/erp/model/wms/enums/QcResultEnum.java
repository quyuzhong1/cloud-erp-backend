package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 质检结果枚举
 * @author Lambda
 * @Classname QcResultEnum
 * @Description TODO
 * @Date 2023-04-17 9:59
 * @Created by yl
 */
public enum QcResultEnum {

    CONFORMITY ("conformity", "合格"),
    NON_CONFORMITY("nonConformity", "不合格");

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

    QcResultEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
