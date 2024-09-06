package com.erp.model.mrp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量公式（规则设置） 销量默认类型 枚举
 * </p>
 *
 * @author will
 * @since 2024-08-23 15:05:16
 */
public enum CfgRuleSalesFormulaDefaultTypeEnum implements EnumMessage {
	DYNAMIC("dynamic", "动态"),
    FIXED("fixed", "固态"),
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

    CfgRuleSalesFormulaDefaultTypeEnum(String code, String name) {
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
        for (CfgRuleSalesFormulaDefaultTypeEnum statusEnum : CfgRuleSalesFormulaDefaultTypeEnum.values()) {
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
        for (CfgRuleSalesFormulaDefaultTypeEnum statusEnum : CfgRuleSalesFormulaDefaultTypeEnum.values()) {
            if (statusEnum.getName().equals(name)) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
}
