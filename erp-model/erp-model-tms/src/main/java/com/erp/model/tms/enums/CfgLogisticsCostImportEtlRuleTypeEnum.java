package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段清洗规则类型枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportEtlRuleTypeEnum implements EnumMessage {
    REPLACE("replace", "字符替换"),
    SUBSTRING("substring", "字段截取"),
    TO_POSITIVE("toPositive", "转化数值为正数"),
    TO_NEGATIVE("toNegative", "转化数值为负数"),
    FILL_EMPTY("fillEmpty", "为空填充"),
    ;

    /**
     * 类型编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 构建字段清洗规则类型枚举。
     *
     * @param code 类型编码
     * @param name 类型名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportEtlRuleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取类型编码。
     *
     * @param 无
     * @return 类型编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取类型名称。
     *
     * @param 无
     * @return 类型名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取类型名称。
     *
     * @param code 类型编码
     * @return 类型名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportEtlRuleTypeEnum typeEnum : CfgLogisticsCostImportEtlRuleTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
