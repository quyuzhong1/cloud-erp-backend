package com.erp.model.dmp.enums;

/**
 * @author CLOUD
 * @version 1.0

 * @date 2023/3/10 9:46
 */
public enum SalesDataReportEnum {

    CREATE_DAY_REPORT("dmp_order_create_report_all_day_physical()", "创建时间日报表"),
    CREATE_WEEK_REPORT("dmp_order_create_report_all_week_physical()", "创建时间周报表"),
    CREATE_MONTH_REPORT("dmp_order_create_report_all_month_physical()", "创建时间月报表"),
    CREATE_QUARTER_REPORT("dmp_order_create_report_all_quarter_physical()", "创建时间季报表"),
    CREATE_YEAR_REPORT("dmp_order_create_report_all_year_physical()", "创建时间年报表"),
    DELIVERY_DAY_REPORT("dmp_order_delivery_report_all_day_physical()", "发货时间日报表"),
    DELIVERY_WEEK_REPORT("dmp_order_delivery_report_all_week_physical()", "发货时间周报表"),
    DELIVERY_MONTH_REPORT("dmp_order_delivery_report_all_month_physical()", "发货时间月报表"),
    DELIVERY_QUARTER_REPORT("dmp_order_delivery_report_all_quarter_physical()", "发货时间季报表"),
    DELIVERY_YEAR_REPORT("dmp_order_delivery_report_all_year_physical()", "发货时间年报表"),
    ;

    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    SalesDataReportEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
