package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 预入库单认领状态枚举
 * 主表与详情表的 claim_status 字段均使用此枚举，表存字符串值
 */
public enum PrestockClaimStatusEnum implements EnumMessage {

    UNLINKED("UNLINKED", "未关联"),
    PARTIAL("PARTIAL", "部分关联"),
    LINKED("LINKED", "已关联"),
    FORCE_CLOSE("FORCE_CLOSE", "强制关闭"),
    ;

    @EnumValue
    @JsonValue
    private final String status;
    private final String name;

    PrestockClaimStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    /**
     * 根据 status 获取名称
     */
    public static String getName(String status) {
        if (StringUtils.isBlank(status)) {
            return "";
        }
        for (PrestockClaimStatusEnum item : PrestockClaimStatusEnum.values()) {
            if (status.equals(item.getStatus())) {
                return item.getName();
            }
        }
        return "";
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return status;
    }

    @Override
    public String getName() {
        return name;
    }
}
