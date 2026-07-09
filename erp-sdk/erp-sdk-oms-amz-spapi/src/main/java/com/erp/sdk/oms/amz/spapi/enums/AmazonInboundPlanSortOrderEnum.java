package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 亚马逊 SP-API FBA Inbound Plan 列表排序方向
 * <p>
 * 对应 {@code listInboundPlans} sortOrder 参数。
 */
@Getter
@AllArgsConstructor
public enum AmazonInboundPlanSortOrderEnum {

    ASC("ASC", "升序"),
    DESC("DESC", "降序"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static AmazonInboundPlanSortOrderEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        for (AmazonInboundPlanSortOrderEnum item : values()) {
            if (item.code.equals(normalized)) {
                return item;
            }
        }
        return null;
    }
}
