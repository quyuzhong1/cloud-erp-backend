package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 亚马逊 SP-API FBA Inbound Plan 列表排序字段
 * <p>
 * 对应 {@code listInboundPlans} sortBy 参数。
 */
@Getter
@AllArgsConstructor
public enum AmazonInboundPlanSortByEnum {

    CREATION_TIME("CREATION_TIME", "创建时间"),
    LAST_UPDATED_TIME("LAST_UPDATED_TIME", "最后更新时间"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static AmazonInboundPlanSortByEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        for (AmazonInboundPlanSortByEnum item : values()) {
            if (item.code.equals(normalized)) {
                return item;
            }
        }
        return null;
    }
}
