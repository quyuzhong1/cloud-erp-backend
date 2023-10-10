package com.erp.model.oms.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author Lambda
 * @Classname RuleOrderFlowStatus
 * @Description 订单规则流转状态
 * @Date 2023-09-06 15:32
 * @Created by yl
 */
public enum RuleOrderFlowStatusEnum {

    WAIT_DISTRIBUTION("waitDistribution",  "待配货"),

    IN_DISTRIBUTION("inDistribution",  "配货中"),;


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



    RuleOrderFlowStatusEnum(String code, String name) {
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
