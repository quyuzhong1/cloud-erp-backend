package com.erp.server.bi.enums;

/**
 *  bi 专题 筛选条件
 * @Classname
 * @Description TODO
 * @Date 2022-12-15 19:47
 * @Created by yl
 */
public enum BiAdvancedFilterEnum {

    BUSINESS_DIVISION("事业部","businessDivision"),
    PLATFORM("平台","platform"),
    SITE("站点","site"),
    SHOP("店铺","shop"),
    CATEGORY("品类","category"),
    BRAND("品牌","brand"),
    SKU("sku","sku"),
    USER_ID("人员","userId"),
    REFUND_TIME("退货时间","refundTime"),
    TODAY("今天","today"),
    YESTERDAY("昨天","yesterday"),
    LAST_SEVEN_DAYS("最近7天","lastSevenDays"),
    LAST_FIFTEEN_DAYS("最近15天","lastFifteenDays"),
    LAST_THIRTY_DAYS("最近30天","lastThirtyDays");



    private String searchType;

    private String name;



    BiAdvancedFilterEnum(String name, String searchType) {
        this.name = name;
        this.searchType = searchType;

    }

    public String getFlag() {
        return searchType;
    }

    public String getName() {
        return name;
    }
}
