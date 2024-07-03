package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 输出黑名单 数据类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-07-03 16:04:42
 */
public enum DmpCfgOutputBlackDataTypeEnum implements EnumMessage {
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

    DmpCfgOutputBlackDataTypeEnum(String code, String name) {
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
        for (DmpCfgOutputBlackDataTypeEnum statusEnum : DmpCfgOutputBlackDataTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
