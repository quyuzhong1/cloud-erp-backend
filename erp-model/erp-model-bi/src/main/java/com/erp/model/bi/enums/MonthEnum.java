package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 考核维度
 * @author Lambda
 * @Classname MetricsEnums
 * @Description TODO
 * @Date 2023-09-13 10:38
 * @Created by yl
 */
public enum MonthEnum {

    JANUARY("january",1),
    FEBRUARY("february",2),
    MARCH("march",3),
    APRIL("april",4),
    MAY("may",5),
    JUNE("june",6),
    JULY("july",7),
    AUGUST("august",8),
    SEPTEMBER("september",9),
    OCTOBER("october",10),
    NOVEMBER("november",11),
    DECEMBER("december",12)

    ;


    MonthEnum(String code, Integer value) {
        this.code = code;
        this.value = value;
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
    private Integer value;


    public String getCode() {
        return code;
    }


    public Integer getValue() {
        return value;
    }
}
