package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 14:12
 */
public enum InvalidStatusEnum {

    NOT_VOIDED(false, "未作废"),
    VOIDED(true, "已作废");

    private Boolean status;
    private String name;

    InvalidStatusEnum(Boolean status, String name) {
        this.status = status;
        this.name = name;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String getName(Boolean status) {
        if (ObjectUtils.isNotEmpty(status)) {
            for (InvalidStatusEnum item : InvalidStatusEnum.values()) {
                if (status.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
