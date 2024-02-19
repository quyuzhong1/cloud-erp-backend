package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname PackageForecastCollectModeEnum
 * @Description TODO
 * @Date 2024-02-19 11:23
 * @Created by yl
 */
public enum PackageForecastCollectModeEnum implements EnumMessage {
    TO_HOME("toHome", "上门揽收"),
    SELF_SEND("selfSend", "自送"),
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

    PackageForecastCollectModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
