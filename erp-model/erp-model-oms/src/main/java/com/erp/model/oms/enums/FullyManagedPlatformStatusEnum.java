package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销售订单-tiktok全托管属性表 平台订单来源 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-03-24 17:08:06
 */
public enum FullyManagedPlatformStatusEnum implements EnumMessage {
    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    SHIPPED("shipped", "已发货"),
    WAIT_RECEIVE("waitReceive", "待收货"),
    RECEIVED("received", "已收货"),
    WAIT_QC("waitQc", "待质检"),
    WAIT_INSTOCK("waitInstock", "待上架"),
    RETURN("return", "已退供"),
    SOTOCK_IN("stockIn", "已入库"),
    INVAILD("invalid", "已作废"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    FullyManagedPlatformStatusEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FullyManagedPlatformStatusEnum statusEnum : FullyManagedPlatformStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
