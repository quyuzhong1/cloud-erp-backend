package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * AWD标签类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-12-24 18:59:43
 */
public enum WmsAwdPageTypeEnum implements EnumMessage {
	THERMAL_NONPCP("THERMAL_NONPCP", "热敏纸一个标签"),
	PLAIN_PAPER("PLAIN_PAPER", "每张美国信纸1个标签"),
	LETTER_6("LETTER_6", "每张美国信纸6个标签"),
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

    WmsAwdPageTypeEnum(String code, String name) {
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
        for (WmsAwdPageTypeEnum statusEnum : WmsAwdPageTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
