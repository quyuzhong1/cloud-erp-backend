package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 18:30
 */
public enum ProductPlanProcessEnum {

    NEW_PRODUCT_PLAN("newProductPlan", "新建规划",""),
    PRODUCT_DEVELOP("productDevelop", "转产品开发",""),
    PROJECT_STARTUP("projectStartup", "项目启动",""),
    PROJECT_COMPLETE("projectComplete", "项目完成","");


    private String code;

    private String name;

    private String desc;

    ProductPlanProcessEnum(String code, String name,String desc) {
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

    public static ProductPlanProcessEnum getNameByCode(String code) {
        ProductPlanProcessEnum[] enums = values();
        for (ProductPlanProcessEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }

}
