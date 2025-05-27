package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 流程设置执行条件 流程类型 枚举
 * </p>
 *
 * @author hcg
 * @since 2025-05-13 09:43:58
 */
public enum CfgProcessRuleTypeEnum implements EnumMessage {
	ERPPROCESS("erpProcess", "ERP流程"),
	FSPROCESS("fsProcess", "飞书流程"),
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

    CfgProcessRuleTypeEnum(String code, String name) {
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
        for (CfgProcessRuleTypeEnum statusEnum : CfgProcessRuleTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static CfgProcessRuleTypeEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CfgProcessRuleTypeEnum statusEnum : CfgProcessRuleTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
