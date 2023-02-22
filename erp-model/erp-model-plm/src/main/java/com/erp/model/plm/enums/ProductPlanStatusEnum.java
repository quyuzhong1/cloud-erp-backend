package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 17:57
 */
public enum ProductPlanStatusEnum {

    NOT_STARTED("1", "未开始",""),
    UNDER_INVESTIGATION("2", "调研中",""),
    PROJECT_APPROVED("3", "已立项",""),
    PROJECT_STARTUP("4", "项目启动",""),
    PROJECT_PROGRESS("5", "项目进行中",""),
    PROJECT_COMPLETE("6", "项目完成","");


    private String code;

    private String name;

    private String desc;

    ProductPlanStatusEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static ProductPlanStatusEnum getNameByCode(String code) {
        ProductPlanStatusEnum[] enums = values();
        for (ProductPlanStatusEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }
    public static ProductPlanStatusEnum getByName(String name) {
        ProductPlanStatusEnum[] enums = values();
        for (ProductPlanStatusEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum;
            }
        }
        return null;
    }

}
