package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @description: 目标完成查看类型
 * @date 2023/9/14 17:36
 */
public enum TargetFinishViewTypeEnum implements EnumMessage {

    FINISH_RATE("finishRate","完成率"),
    RATIO("ratio","占比")
    ;


    TargetFinishViewTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        TargetFinishViewTypeEnum[] enums = values();
        for (TargetFinishViewTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return null;
    }
}
