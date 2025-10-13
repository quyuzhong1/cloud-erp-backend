package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 模具档案 模具标识 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-10 14:03:43
 */
public enum MoldInfoTagEnum implements EnumMessage {
	FIRST("first", "首套模"),
	COPY("copy", "复制模"),
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

    MoldInfoTagEnum(String code, String name) {
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
        for (MoldInfoTagEnum statusEnum : MoldInfoTagEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (MoldInfoTagEnum statusEnum : MoldInfoTagEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
}
