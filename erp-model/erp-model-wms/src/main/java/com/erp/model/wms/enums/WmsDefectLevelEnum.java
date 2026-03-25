package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 缺陷等级 枚举
 * </p>
 *
 * @author wtr
 * @since 2026-03-25 11:34:33
 */
public enum WmsDefectLevelEnum implements EnumMessage {
	GENERALDEFECT("generalDefect", "一般缺陷"),
	CRITICALDEFECT("criticalDefect", "严重缺陷"),
	FATALDDEFECT("fataldDefect", "致命缺陷"),
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

    WmsDefectLevelEnum(String code, String name) {
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
        for (WmsDefectLevelEnum statusEnum : WmsDefectLevelEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
