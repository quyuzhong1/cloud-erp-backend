package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @description: 分配类型枚举
 * @author Will
 * @date: 2023/1/9 12:28
 */
public enum DistributionTypeEnum implements EnumMessage {

    DISTRIBUTION_ROLE(0,"按角色分配"),
    DISTRIBUTION_USER(1,"按人员分配"),
    DISTRIBUTION_SUPERIOR(2,"上级人员负责人");

    private Integer code;

    private String name;


    DistributionTypeEnum(Integer code, String name) {
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
        for (DistributionTypeEnum state : DistributionTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
