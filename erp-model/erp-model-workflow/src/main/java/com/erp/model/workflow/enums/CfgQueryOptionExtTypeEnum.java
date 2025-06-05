package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * cfg_query_option拓展表 字段类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-04 16:36:08
 */
public enum CfgQueryOptionExtTypeEnum implements EnumMessage {
	TEXT("text", "文字"),
	BOOL("bool", "布尔值"),
	CLASS("class", "类"),
	ENUM("enum", "枚举"),
	USER("user", "用户"),
	DEPT("dept", "部门"),
	DICT("dict", "字典"),
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

    CfgQueryOptionExtTypeEnum(String code, String name) {
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
        for (CfgQueryOptionExtTypeEnum statusEnum : CfgQueryOptionExtTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
