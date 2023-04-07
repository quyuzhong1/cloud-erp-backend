package com.erp.model.scm.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description:
 * @date 2023/3/21 17:50
 */
public enum CreatePoTypeEnum {

    NOT_GENERATED("0", "未生成"),
    PARTIAL_GENERATED("1", "部分生成"),
    ALL_GENERATED("2", "已生成");

    private String status;
    private String name;

    CreatePoTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (CreatePoTypeEnum item : CreatePoTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
