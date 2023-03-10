package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/6 17:31
 */
public enum RelatedSkuTypeEnum implements EnumMessage {

    ALL_RELATED("1","自动关联"),
    CHOICE_RELATED("2","选择关联"),
    NOT_RELATED("3","不关联");

    private String code;

    private String name;


    RelatedSkuTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (RelatedSkuTypeEnum state : RelatedSkuTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
