package com.erp.server.bi.enums;

import lombok.Getter;

/**
 * @Classname  仪表盘相关枚举

 * @Date 2022-09-29 14:13
 * @Created by yl
 */
@Getter
public enum DateTypeEnum {

    DAY("day","DAY","日"),
    WEEK("week","WEEK","周"),
    MONTH("month","MONTH","月"),
    QUARTER("quarter","QUARTER","季度"),
    YEAR("year","YEAR","年"),
    ;



    private final String code;
    /**
     * 大写
     */
    private final String type;
    private final String name;


    DateTypeEnum(String code, String type, String name) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

}
