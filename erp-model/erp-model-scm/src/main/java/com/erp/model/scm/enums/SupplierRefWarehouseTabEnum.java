package com.erp.model.scm.enums;

/**
 * 仓库绑定tab枚举
 * @author will
 * @date 2025/6/18 18:21
 */
public enum SupplierRefWarehouseTabEnum {

    DISABLED(true, "禁用"),
    ENABLE(false, "启用");
    ;

    private Boolean code;
    private String name;

    SupplierRefWarehouseTabEnum(Boolean code, String name) {
        this.code = code;
        this.name = name;
    }

    public Boolean getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Boolean code) {
        SupplierRefWarehouseTabEnum[] enums = values();
        for (SupplierRefWarehouseTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum.getName();
            }
        }
        return null;
    }

    public static Boolean getCodeByName(String name) {
        SupplierRefWarehouseTabEnum[] enums = values();
        for (SupplierRefWarehouseTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getName().equals(name)) {
                return productTaskCategoryEnum.getCode();
            }
        }
        return null;
    }

    public static SupplierRefWarehouseTabEnum getEnumByType(Boolean code){
        SupplierRefWarehouseTabEnum[] enums = values();
        for (SupplierRefWarehouseTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum;
            }
        }
        return null;
    }
}
