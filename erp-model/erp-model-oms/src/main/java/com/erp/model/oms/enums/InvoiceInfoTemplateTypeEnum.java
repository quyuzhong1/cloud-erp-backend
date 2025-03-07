package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 上传记录 模板类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-03-07 14:15:46
 */
public enum InvoiceInfoTemplateTypeEnum implements EnumMessage {
	ERP("erp", "ERP模板"),
	OFFICIAL("official", "官方模板"),
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

    InvoiceInfoTemplateTypeEnum(String code, String name) {
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
        for (InvoiceInfoTemplateTypeEnum statusEnum : InvoiceInfoTemplateTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
