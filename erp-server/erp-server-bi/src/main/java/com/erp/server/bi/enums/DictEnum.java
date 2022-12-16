package com.erp.server.bi.enums;

/**
 * @Classname DictEnum
 * @Description TODO
 * @Date 2022-12-08 18:51
 * @Created by yl
 */
public enum DictEnum {

    DASHBOARD("个人仪表盘","subjectCategory","dashboard"),
    MODULE("模块分类","moduleCategory",""),
    DATASOURCECOST("数据源成本","dataSourceCost","");

    private String name;

    private String type;

    private String value;


    DictEnum(String name, String type,String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
}
