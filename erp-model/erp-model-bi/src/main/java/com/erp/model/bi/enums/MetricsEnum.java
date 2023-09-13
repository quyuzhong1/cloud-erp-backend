package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 考核维度
 * @author Lambda
 * @Classname MetricsEnums
 * @Description TODO
 * @Date 2023-09-13 10:38
 * @Created by yl
 */
public enum MetricsEnum implements EnumMessage {

    SALES_AMOUNT("salesAmount","销售额"),
    FINANCE_SALES_AMOUNT("financeSalesAmount","财务销售额"),
    GROSS_PROFIT("grossProfit","毛利润"),
    GROSS_PROFIT_RATE("grossProfitRate","毛利率")
    ;


    MetricsEnum(String code, String name) {
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
}
