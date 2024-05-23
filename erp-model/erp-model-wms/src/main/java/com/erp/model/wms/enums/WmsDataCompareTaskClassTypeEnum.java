package com.erp.model.wms.enums;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <p>
 * 数据对比任务 单据类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-03-20 17:21:53
 */
public enum WmsDataCompareTaskClassTypeEnum implements EnumMessage {
	INT("int", "数字"),
	STRING("string", "字符"),
	DATE("date", "日期"),
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

    WmsDataCompareTaskClassTypeEnum(String code, String name) {
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
        for (WmsDataCompareTaskClassTypeEnum statusEnum : WmsDataCompareTaskClassTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
