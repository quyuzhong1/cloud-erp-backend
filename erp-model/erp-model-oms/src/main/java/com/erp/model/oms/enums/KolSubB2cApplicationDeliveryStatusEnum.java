package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * B2C寄样申请单拆分单 发货状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-12-04 10:07:54
 */
public enum KolSubB2cApplicationDeliveryStatusEnum implements EnumMessage {
	WAITSHIPPED("waitShipped", "待发货"),
	SHIPPED("shipped", "已发货"),
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

    KolSubB2cApplicationDeliveryStatusEnum(String code, String name) {
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
        for (KolSubB2cApplicationDeliveryStatusEnum statusEnum : KolSubB2cApplicationDeliveryStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
