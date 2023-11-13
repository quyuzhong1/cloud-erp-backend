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

    REJECT("reject",  "审核不通过"),

    PASS("pass",  "审核通过"),;


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
