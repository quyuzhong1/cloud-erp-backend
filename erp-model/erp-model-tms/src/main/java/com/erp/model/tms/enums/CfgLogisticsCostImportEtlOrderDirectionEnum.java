package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段清洗顺序截取方向枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportEtlOrderDirectionEnum implements EnumMessage {
    LEFT("left", "从左截取"),
    RIGHT("right", "从右截取"),
    ;

    /**
     * 方向编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 方向名称
     */
    private String name;

    /**
     * 构建顺序截取方向枚举。
     *
     * @param code 方向编码
     * @param name 方向名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportEtlOrderDirectionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取方向编码。
     *
     * @param 无
     * @return 方向编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取方向名称。
     *
     * @param 无
     * @return 方向名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取方向名称。
     *
     * @param code 方向编码
     * @return 方向名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportEtlOrderDirectionEnum directionEnum : CfgLogisticsCostImportEtlOrderDirectionEnum.values()) {
            if (code.equals(directionEnum.getCode())) {
                return directionEnum.getName();
            }
        }
        return "";
    }
}
