package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 质检类型枚举
 *
 * @author Lambda
 * @Classname QcTypeEnum

 * @Date 2023-04-13 10:34
 * @Created by yl
 */
public enum PlanTypeEnum implements EnumMessage {
    FIXED("fixed", "固定样品量"),
    RATE("rate", "百分比抽样"),
    GB("gb", "国标AQL表"),
    ALL("all", "全检"),
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


    PlanTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getByCode(String code) {
        PlanTypeEnum qcTypeEnum = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcTypeEnum != null) {
            return qcTypeEnum.getName();
        }
        return "";
    }
}
