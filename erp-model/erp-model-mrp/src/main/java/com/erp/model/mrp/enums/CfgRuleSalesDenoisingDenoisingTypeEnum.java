package com.erp.model.mrp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量去噪信息 去噪类型，percentage百分比去噪 枚举
 * </p>
 *
 * @author will
 * @since 2024-08-23 15:05:16
 */
public enum CfgRuleSalesDenoisingDenoisingTypeEnum implements EnumMessage {
    PERCENTAGE("percentage", "百分比去噪"),
	FIXED_VALUE("fixedValue", "固定值去噪"),
	COMPLETELY("completely", "完全去噪"),
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

    CfgRuleSalesDenoisingDenoisingTypeEnum(String code, String name) {
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
        for (CfgRuleSalesDenoisingDenoisingTypeEnum statusEnum : CfgRuleSalesDenoisingDenoisingTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
