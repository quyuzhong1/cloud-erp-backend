package com.sdk.wms.tongyou.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 通邮查询派送订单状态。
 *
 * @author will
 */
@Getter
public enum TongYouOutboundStatusEnum {
    DRAFT("0", "草稿箱", false),
    WAIT_CONFIRM("1", "待确认", false),
    WAIT_SHIPPED("2", "待发货", false),
    PACKED("3", "已打包", false),
    SHIPPED("4", "已发货", true),
    REVIEW_FAILED("5", "审核不通过", false),
    SIGNED("6", "已签收", true),
    INTERCEPTED("7", "已拦截", false),
    ;

    private static final String UNKNOWN_STATUS_TEXT = "未知";

    private final String code;
    private final String name;
    private final boolean shippedStatus;

    TongYouOutboundStatusEnum(String code, String name, boolean shippedStatus) {
        this.code = code;
        this.name = name;
        this.shippedStatus = shippedStatus;
    }

    public static TongYouOutboundStatusEnum getByCode(String code) {
        if (isBlank(code)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> code.equals(item.getCode()))
                .findFirst()
                .orElse(null);
    }

    public static boolean isShippedStatus(String code) {
        TongYouOutboundStatusEnum statusEnum = getByCode(code);
        return statusEnum != null && statusEnum.isShippedStatus();
    }

    public static String getDisplayText(String code) {
        if (isBlank(code)) {
            return UNKNOWN_STATUS_TEXT;
        }
        TongYouOutboundStatusEnum statusEnum = getByCode(code);
        if (statusEnum == null) {
            return code;
        }
        return statusEnum.getName() + "(" + statusEnum.getCode() + ")";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
