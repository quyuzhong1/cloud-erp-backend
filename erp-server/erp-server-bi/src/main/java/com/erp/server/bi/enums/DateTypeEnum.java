package com.erp.server.bi.enums;

import lombok.Getter;

/**
 * @Classname  仪表盘相关枚举

 * @Date 2022-09-29 14:13
 * @Created by yl
 */
@Getter
public enum DateTypeEnum {

    DAY("day","日"),
    WEEK("week","周"),
    MONTH("month","月"),
    QUARTER("quarter","季度"),
    YEAR("year","年"),
    ;



    private final String code;

    private final String name;


    DateTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
