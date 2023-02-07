package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/6 17:31
 */
public enum RelatedSkuTypeEnum {

    ALL_ASSOCIATION(1,"自动关联"),
    CHOICE_ASSOCIATION(2,"选择关联"),
    NOT_ASSOCIATION(3,"不关联");

    private Integer code;

    private String name;


    RelatedSkuTypeEnum(Integer code, String name) {
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
        for (RelatedSkuTypeEnum state : RelatedSkuTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
