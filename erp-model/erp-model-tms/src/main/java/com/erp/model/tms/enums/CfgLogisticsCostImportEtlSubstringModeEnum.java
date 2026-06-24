package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段清洗字段截取模式枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportEtlSubstringModeEnum implements EnumMessage {
    BY_SYMBOL("bySymbol", "按照符号截取"),
    BY_ORDER("byOrder", "按照顺序截取"),
    CHINESE("chinese", "按照中文字符截取"),
    ENGLISH("english", "按照英文字母截取"),
    ;

    /**
     * 模式编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 模式名称
     */
    private String name;

    /**
     * 构建字段截取模式枚举。
     *
     * @param code 模式编码
     * @param name 模式名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportEtlSubstringModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取模式编码。
     *
     * @param 无
     * @return 模式编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取模式名称。
     *
     * @param 无
     * @return 模式名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取模式名称。
     *
     * @param code 模式编码
     * @return 模式名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportEtlSubstringModeEnum modeEnum : CfgLogisticsCostImportEtlSubstringModeEnum.values()) {
            if (code.equals(modeEnum.getCode())) {
                return modeEnum.getName();
            }
        }
        return "";
    }
}
