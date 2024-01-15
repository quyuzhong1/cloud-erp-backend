package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/30 16:01
 */
public enum SupplierTabEnum {
    ALL_TASK("all", "全部"),
    TO_ME_CHECK_TASK("waitMe", "待我审核"),
    APPROVE("approve", "已审核"),
    REJECT("reject", "不通过"),
    ;

    private String code;
    private String name;

    SupplierTabEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        SupplierTabEnum[] enums = values();
        for (SupplierTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum.getName();
            }
        }
        return null;
    }

    public static SupplierTabEnum getEnumByType(String code){
        SupplierTabEnum[] enums = values();
        for (SupplierTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum;
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        SupplierTabEnum[] enums = values();
        for (SupplierTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getName().equals(name)) {
                return productTaskCategoryEnum.getCode();
            }
        }
        return null;
    }
}
