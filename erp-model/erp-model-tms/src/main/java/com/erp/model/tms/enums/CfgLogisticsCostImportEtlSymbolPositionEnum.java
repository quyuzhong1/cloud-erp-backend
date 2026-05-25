package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段清洗符号截取位置枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportEtlSymbolPositionEnum implements EnumMessage {
    BEFORE("before", "符号前"),
    AFTER("after", "符号后"),
    ;

    /**
     * 位置编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 位置名称
     */
    private String name;

    /**
     * 构建符号截取位置枚举。
     *
     * @param code 位置编码
     * @param name 位置名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportEtlSymbolPositionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取位置编码。
     *
     * @param 无
     * @return 位置编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取位置名称。
     *
     * @param 无
     * @return 位置名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取位置名称。
     *
     * @param code 位置编码
     * @return 位置名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportEtlSymbolPositionEnum positionEnum : CfgLogisticsCostImportEtlSymbolPositionEnum.values()) {
            if (code.equals(positionEnum.getCode())) {
                return positionEnum.getName();
            }
        }
        return "";
    }
}
