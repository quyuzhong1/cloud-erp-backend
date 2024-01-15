package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/30 16:01
 */
public enum SupplierTabEnum {
    ALL_TASK(0, "全部"),
    TO_ME_CHECK_TASK(1, "待我审核"),
    APPROVE(2, "已审核"),
    REJECT(3, "不通过"),
    ;

    private Integer code;
    private String name;

    SupplierTabEnum(Integer code, String name) {
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
        SupplierTabEnum[] enums = values();
        for (SupplierTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode() == code) {
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

    public static Integer getCodeByName(String name) {
        SupplierTabEnum[] enums = values();
        for (SupplierTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getName().equals(name)) {
                return productTaskCategoryEnum.getCode();
            }
        }
        return null;
    }
}
