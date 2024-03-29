package com.erp.model.sys.enums;

/**
 * 金蝶辅助资料类型
 * @author Lambda
 * @Classname KingdeeBusinessOperatorTypeEnum
 * @Date 2023-07-10 10:01
 * @Created by yl
 */
public enum KingdeeAssistDataTypeEnum {

    AREA("0101", "区域"),
    COUNTRY("Country", "国家"),
    PROVINCES("Provinces", "省份"),
    CITY("Citys", "市"),
    ;

    private String code;
    private String name;

    KingdeeAssistDataTypeEnum(String code, String name) {
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
