package com.erp.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/17 9:27
 */
public enum SkuApproveConfigureEnum {

    FIRST_APPROVE(1, "firstApproveIdList",""),
    SECOND_APPROVE(2, "secondApproveIdList","产品部"),
    THIRD_APPROVE(3, "thirdApproveIdList","品质部,采购开发组,采购执行组,物流部"),
    FOURTH_APPROVE(4, "fourthApproveIdList","产品研发中心,供应链中心"),
    FIVE_APPROVE(5, "fiveApproveIdList","产品研发中心");

    private Integer code;
    private String name;
    private String desc;

    SkuApproveConfigureEnum(Integer code, String name, String desc) {
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

    public static String getNameByCode(Integer code) {
        SkuApproveConfigureEnum[] enums = values();
        for (SkuApproveConfigureEnum skuApproveConfigureEnum : enums) {
            if (skuApproveConfigureEnum.getCode() == code) {
                return skuApproveConfigureEnum.getName();
            }
        }
        return null;
    }

    public static SkuApproveConfigureEnum getEnumByType(String code){
        SkuApproveConfigureEnum[] enums = values();
        for (SkuApproveConfigureEnum skuApproveConfigureEnum : enums) {
            if (skuApproveConfigureEnum.getCode().equals(code)) {
                return skuApproveConfigureEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        SkuApproveConfigureEnum[] enums = values();
        for (SkuApproveConfigureEnum skuApproveConfigureEnum : enums) {
            if (skuApproveConfigureEnum.getName().equals(name)) {
                return skuApproveConfigureEnum.getCode();
            }
        }
        return null;
    }
}
