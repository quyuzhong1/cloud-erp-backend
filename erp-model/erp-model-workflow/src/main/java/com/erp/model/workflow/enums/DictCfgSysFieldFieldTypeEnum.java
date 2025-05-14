package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 数大臣单据字段 字段类型 枚举
 * </p>
 *
 * @author hcg
 * @since 2025-05-12 18:18:13
 */
public enum DictCfgSysFieldFieldTypeEnum implements EnumMessage {
	CHECKBOXV2("checkboxV2", "多选"),
	RADIOV2("radioV2", "单选"),
	INPUT("input", "单行文本"),
    TEXTAREA("textarea", "多行文本"),
	DATETIME("datetime", "日期"),
	NUMBER("number", "数值"),
	AMOUNT("amount", "金额"),
    ATTACHMENTV2("attachmentV2","附件"),
    FIELDLIST("fieldList", "明细")
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

    DictCfgSysFieldFieldTypeEnum(String code, String name) {
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
        for (DictCfgSysFieldFieldTypeEnum statusEnum : DictCfgSysFieldFieldTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
