package com.erp.model.wms.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/18 11:30
 */
public enum ViewQcTrendEnum {

    DAY("day", "日"),
    WEEK("week", "周"),
    MONTH("month", "月"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    ViewQcTrendEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (ViewQcTrendEnum viewQcTrendEnum : ViewQcTrendEnum.values()) {
            if (code.equals(viewQcTrendEnum.getCode())) {
                return viewQcTrendEnum.name();
            }
        }
        return "";
    }
}
