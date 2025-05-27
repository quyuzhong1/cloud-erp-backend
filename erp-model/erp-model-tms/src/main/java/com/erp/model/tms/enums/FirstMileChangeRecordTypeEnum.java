package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 头程调整记录 操作类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-05-12 15:19:23
 */
public enum FirstMileChangeRecordTypeEnum implements EnumMessage {
	MANUAL("manual", "人工调整"),
	AUTO("auto", "系统计算"),
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

    FirstMileChangeRecordTypeEnum(String code, String name) {
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
        for (FirstMileChangeRecordTypeEnum statusEnum : FirstMileChangeRecordTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
