package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊 SP-API FBA Inbound Plan 状态
 * <p>
 * 对应 {@code listInboundPlans} status 参数。
 */
@Getter
@AllArgsConstructor
public enum AmazonInboundPlanStatusEnum {

    ACTIVE("ACTIVE", "进行中"),
    VOIDED("VOIDED", "已作废"),
    SHIPPED("SHIPPED", "已发货"),
    ERRORED("ERRORED", "异常"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    public static AmazonInboundPlanStatusEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        for (AmazonInboundPlanStatusEnum item : values()) {
            if (item.code.equals(normalized)) {
                return item;
            }
        }
        return null;
    }

    public static List<String> defaultSyncStatusCodes() {
        return Arrays.asList(ACTIVE.code, SHIPPED.code);
    }

    public static List<String> toCodes(List<AmazonInboundPlanStatusEnum> statusList) {
        if (statusList == null || statusList.isEmpty()) {
            return Collections.emptyList();
        }
        return statusList.stream().map(AmazonInboundPlanStatusEnum::getCode).collect(Collectors.toList());
    }
}
