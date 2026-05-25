package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段单位属性枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportFieldUnitTypeEnum implements EnumMessage {
    WEIGHT("weight", "重量"),
    LENGTH("length", "长度"),
    AMOUNT("amount", "金额"),
    ;

    /**
     * 属性编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 构建字段单位属性枚举。
     *
     * @param code 属性编码
     * @param name 属性名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportFieldUnitTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取属性编码。
     *
     * @param 无
     * @return 属性编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取属性名称。
     *
     * @param 无
     * @return 属性名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取属性名称。
     *
     * @param code 属性编码
     * @return 属性名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportFieldUnitTypeEnum typeEnum : CfgLogisticsCostImportFieldUnitTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
