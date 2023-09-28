package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 考核维度
 *
 * @author zdy
 * @Classname SalePriceRangeEnum
 * @Description 销售单价分布接口区间枚举
 * @Date 2023-09-20 10:38
 * @Created by zdy
 */
public enum SalePriceRangeEnum {

    ZERO("0", 0, 100),
    ONE_HUNDRED("100", 100, 200),
    TWO_HUNDRED("200", 200, 300),
    THREE_HUNDRED("300", 300, 400),
    FOUR_HUNDRED("400", 400, 500),
    FIVE_HUNDRED("500", 500, 600),
    SIX_HUNDRED("600", 600, 700),
    SEVEN_HUNDRED("700", 700, -1);

    SalePriceRangeEnum(String code, Integer startValue, Integer endValue) {
        this.code = code;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 开始值
     */
    private Integer startValue;
    /**
     * 结束值
     */
    private Integer endValue;

    public String getCode() {
        return code;
    }


    public Integer getStartValue() {
        return startValue;
    }

    public Integer getEndValue() {
        return endValue;
    }
}
