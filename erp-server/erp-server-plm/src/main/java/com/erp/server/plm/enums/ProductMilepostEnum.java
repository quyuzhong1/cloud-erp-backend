package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 15:23
 */
public enum ProductMilepostEnum {
    START_MILEPOST_MILEPOST(1, "创建"),
    PROJECT_APPROVAL_MILEPOST(2, "项目立项【默认】"),
    TASK_MILEPOST(3, "任务名称"),
    PROJECT_ARCHIVE_MILEPOST(4, "项目归档【默认】");

    private Integer code;
    private String name;

    ProductMilepostEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        ProductMilepostEnum[] productMilepostEnums = values();
        for (ProductMilepostEnum productMilepostEnum : productMilepostEnums) {
            if (productMilepostEnum.getCode() == code) {
                return productMilepostEnum.getName();
            }
        }
        return null;
    }

    public static ProductMilepostEnum getEnumByType(String code){
        ProductMilepostEnum[] productMilepostEnums = values();
        for (ProductMilepostEnum productMilepostEnum : productMilepostEnums) {
            if (productMilepostEnum.getCode().equals(code)) {
                return productMilepostEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        ProductMilepostEnum[] productMilepostEnums = values();
        for (ProductMilepostEnum productMilepostEnum : productMilepostEnums) {
            if (productMilepostEnum.getName().equals(name)) {
                return productMilepostEnum.getCode();
            }
        }
        return null;
    }
}
