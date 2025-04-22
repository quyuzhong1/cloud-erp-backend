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
public enum SoB2cExtendOrderSourceTypeEnum implements EnumMessage {
	PLATFORM("PLATFORM", "平台备货"),
	MERCHANT("MERCHANT", "自主备货"),
	ABNORMAL_REDELIVERY("ABNORMAL_REDELIVERY", "异常补货"),
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

    SoB2cExtendOrderSourceTypeEnum(String code, String name) {
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
        for (SoB2cExtendOrderSourceTypeEnum statusEnum : SoB2cExtendOrderSourceTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
