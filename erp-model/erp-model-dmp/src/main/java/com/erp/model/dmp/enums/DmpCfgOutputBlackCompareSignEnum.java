package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 输出黑名单 比较符 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-07-03 16:04:42
 */
public enum DmpCfgOutputBlackCompareSignEnum implements EnumMessage {
	EQ("eq", "等于"),
	NE("ne", "不等于"),
	IN("in", "包含"),
	GT("gt", "大于"),
	GE("ge", "大于等于"),
	LT("lt", "小于"),
	LE("le", "小于等于"),
	BE("be", "在之间"),
	LIKE("like"  , "匹配"),
    NOTLIKE("notlike"  , "不匹配"),
	ISNULL("isnull" , "为空"),
	ISNOTNULL("isnotnull" , "不为空"),
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

    DmpCfgOutputBlackCompareSignEnum(String code, String name) {
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
        for (DmpCfgOutputBlackCompareSignEnum statusEnum : DmpCfgOutputBlackCompareSignEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
