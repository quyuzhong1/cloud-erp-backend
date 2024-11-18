package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 规则对配置字符串特殊处理枚举
 */
public enum CfgConditionRuleEnum implements EnumMessage {
    //去前后空格和逗号前后空格
    REMOVE_SPACE("removeSpace", "去空格")
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


    CfgConditionRuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgConditionRuleEnum billTypeEnum : CfgConditionRuleEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgConditionRuleEnum billTypeEnum : CfgConditionRuleEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
