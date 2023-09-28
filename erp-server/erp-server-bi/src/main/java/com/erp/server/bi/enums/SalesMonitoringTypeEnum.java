package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/3 19:42
 */
public enum SalesMonitoringTypeEnum {

    DEPT(0, "deptName","部门"),
    USER(1, "chargeName","人员"),
    SHOP(2, "shopName","店铺"),
    CATEGORY(3, "categoryName","品类"),
    SKU(4, "skuNo","SKU"),
    PLATFORM(5, "platform","平台"),
    COUNTRY(6, "countryName","国家");

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
