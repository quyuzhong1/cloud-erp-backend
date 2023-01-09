package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/3 19:42
 */
public enum SalesMonitoringTypeEnum {

    SALESAMOUNTMONITORING(0, "销售额","销售额监控"),
    SALESQTYMONITORING(1, "销量","销量监控"),
    NEWPRODUCTSMONITORING(2, "新品销售额","销售额监控-新品"),
    OLDPRODUCTSMONITORING(3, "老品销售额","销售额监控-老品"),
    BRANDNAMEMONITORING(4, "品牌销售监控","品牌销售监控"),
    CATEGORYMONITORING(5, "品类销售监控","品类销售监控"),
    CHARGENAMEMONITORING(6, "人员销售监控","人员销售监控监控");

    private Integer code;
    private String name;
    private String desc;

    SalesMonitoringTypeEnum(Integer code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static String getName(Integer code) {
        for (SalesMonitoringTypeEnum salesMonitoringTypeEnum : SalesMonitoringTypeEnum.values()) {
            if (salesMonitoringTypeEnum.getCode().equals(code)) {
                return salesMonitoringTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getDesc(Integer code) {
        for (SalesMonitoringTypeEnum salesMonitoringTypeEnum : SalesMonitoringTypeEnum.values()) {
            if (salesMonitoringTypeEnum.getCode().equals(code)) {
                return salesMonitoringTypeEnum.getDesc();
            }
        }
        return "";
    }


    public static Integer getCodeByName(String name) {
        SalesMonitoringTypeEnum[] enums = values();
        for (SalesMonitoringTypeEnum salesMonitoringTypeEnum : enums) {
            if (salesMonitoringTypeEnum.getName().equals(name)) {
                return salesMonitoringTypeEnum.getCode();
            }
        }
        return null;
    }
}
