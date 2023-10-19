package com.erp.model.sys.enums;

/**
 * @author Lambda
 * @Classname KingdeeBusinessOperatorTypeEnum
 * @Date 2023-07-10 10:01
 * @Created by yl
 */
public enum KingdeeBusinessOperatorTypeEnum {

    XSY("XSY", "销售员"),
    CGY("CGY", "采购员"),
    WHY("WHY", "仓管员"),
    ;

    private String code;
    private String name;

    KingdeeBusinessOperatorTypeEnum(String code, String name) {
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
