package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 费用项配置字段基础表 ERP字段类型 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-20 17:53:25
 */
public enum CfgLogisticsCostImportFieldFieldTypeEnum implements EnumMessage {
	INTEGER("Integer", "整数"),
	STRING("String", "字符串"),
    LOCALDATE("LocalDate", "日期"),
    LOCALDATETIME("LocalDateTime", "日期时间"),
    BOOLEAN("Boolean", "布尔值"),
    BIGDECIMAL("BigDecimal", "小数"),


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

    CfgLogisticsCostImportFieldFieldTypeEnum(String code, String name) {
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
        for (CfgLogisticsCostImportFieldFieldTypeEnum statusEnum : CfgLogisticsCostImportFieldFieldTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
