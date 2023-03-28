package com.erp.model.scm.enums;

import com.common.business.enums.ApproveStatusEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 14:12
 */
public enum InvalidStatusEnum {

    NOT_VOIDED("0", "未作废"),
    VOIDED("1", "已作废");

    private String status;
    private String name;

    InvalidStatusEnum(String status, String name) {
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
            for (InvalidStatusEnum item : InvalidStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
