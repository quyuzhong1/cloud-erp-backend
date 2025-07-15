package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 字段类型 枚举
 * </p>
 *
 * @author hcg
 * @since 2025-05-15 12:14:18
 */
public enum CfgQueryOptionFieldTypeEnum implements EnumMessage {
	CHECKBOXV2("checkboxV2", "选项-多选"),
	RADIOV2("radioV2", "选项-单选"),
	INPUT("input", "单行文本"),
    TEXTAREA("textarea", "多行文本"),
	DATE("date", "日期"),
	NUMBER("number", "数值"),
	AMOUNT("amount", "金额"),
	ATTACHMENTV2("attachmentV2", "附件"),
	FIELDLIST("fieldList", "明细"),
    IMAGE("image", "图片"),
    IMAGEV2("imageV2", "图片"),
    DEPARTMENT("department", "部门"),
    CONTACT("contact", "联系人"),
    DEFAULT("default","默认值"),
    NULLVALUE("nullValue","设置为空"),
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

    CfgQueryOptionFieldTypeEnum(String code, String name) {
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
        for (CfgQueryOptionFieldTypeEnum statusEnum : CfgQueryOptionFieldTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static CfgQueryOptionFieldTypeEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CfgQueryOptionFieldTypeEnum statusEnum : CfgQueryOptionFieldTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
