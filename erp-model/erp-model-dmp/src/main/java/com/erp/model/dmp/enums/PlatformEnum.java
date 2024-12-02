package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 18:41
 */
public enum PlatformEnum {

    MABANG(1, "mabang", "马帮"),
    GYY(2, "gyy", "管易云"),
    KINGDEE(3, "kingdee", "金蝶云星空"),
    KINGDEE_ECC(4, "kingdeeEcc", "金蝶云星空ECC"),
    ERP(5, "erp", "自研ERP"),
    ERP_OMS(6, "erp-oms", "自研ERP订单系统"),
    ERP_DMP(7, "erp-dmp", "自研ERP数据中台系统"),
    ERP_TMS(7, "erp-tms", "自研ERP数据物流系统"),
    ERP_SYS(8, "erp-sys", "自研ERP基础数据系统"),
    ERP_WMS(9, "erp-wms", "自研ERP仓储系统"),
    LINGXING(10, "lingxing", "领星"),
    WANGDIAN(11,"wangdian","旺店通"),
    SDY(12,"sdy","速帝云"),
    ;

    private Integer code;

    private String name;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    PlatformEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static PlatformEnum getByCode(Integer code) {
        PlatformEnum[] values = values();
        for (PlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static PlatformEnum getByName(String name) {
        PlatformEnum[] values = values();
        for (PlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
