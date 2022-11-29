package com.erp.server.plm.enums;

/**
 * @description: 产品信息状态枚举
 * @author Will
 * @date: 2022/11/28 14:54
 */
public enum ProductDetailStatusEnum {

    WAIT_CONFIRM(0, "待审核"),
    APPROVAL_ING(1, "审核中"),
    APPROVAL_PASS(2, "审核通过"),
    APPROVAL_NO_PASS(3, "审核不通过");


    private Integer code;
    private String name;

    ProductDetailStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ProductDetailStatusEnum state : ProductDetailStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
