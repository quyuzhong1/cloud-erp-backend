package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 17:57
 */
public enum ProductPlanStatusEnum {

    WAIT("1", "未开始",""),
    PROBE("2", "调研中",""),
    APPROVAL("3", "已立项",""),
    YES_START("4", "项目启动",""),
    ING("5", "项目进行中",""),
    FINISH("6", "项目完成",""),
    CANCEL("7", "取消","");

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

    public static String getNameByCode(String code) {
        ProductPlanStatusEnum[] enums = values();
        for (ProductPlanStatusEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum.getName();
            }
        }
        return "";
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
