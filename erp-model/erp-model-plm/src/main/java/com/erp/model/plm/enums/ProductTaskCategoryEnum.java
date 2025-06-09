package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/30 16:01
 */
public enum ProductTaskCategoryEnum {
    ALL_TASK(1, "全部任务"),
    TO_ME_FINISH_TASK(2, "待我完成任务"),
    TO_ME_CHECK_TASK(3, "待我审核任务");

    private Integer code;
    private String name;

    ProductTaskCategoryEnum(Integer code, String name) {
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
        ProductTaskCategoryEnum[] enums = values();
        for (ProductTaskCategoryEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum.getName();
            }
        }
        return null;
    }

    public static ProductTaskCategoryEnum getEnumByType(Integer code){
        ProductTaskCategoryEnum[] enums = values();
        for (ProductTaskCategoryEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        ProductTaskCategoryEnum[] enums = values();
        for (ProductTaskCategoryEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getName().equals(name)) {
                return productTaskCategoryEnum.getCode();
            }
        }
        return null;
    }
}
