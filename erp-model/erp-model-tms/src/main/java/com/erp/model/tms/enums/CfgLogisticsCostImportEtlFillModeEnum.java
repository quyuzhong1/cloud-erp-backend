package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 维护费用配置字段清洗为空填充方式枚举。
 *
 * @author jack
 * @date 2026/05/22
 */
public enum CfgLogisticsCostImportEtlFillModeEnum implements EnumMessage {
    CUSTOM("custom", "自定义"),
    FIELD("field", "按照字段取值-表头取值"),
    ;

    /**
     * 方式编码
     */
    @EnumValue
    @JsonValue
    private String code;

    /**
     * 方式名称
     */
    private String name;

    /**
     * 构建为空填充方式枚举。
     *
     * @param code 方式编码
     * @param name 方式名称
     * @return 无
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    CfgLogisticsCostImportEtlFillModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取方式编码。
     *
     * @param 无
     * @return 方式编码
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取方式名称。
     *
     * @param 无
     * @return 方式名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取方式名称。
     *
     * @param code 方式编码
     * @return 方式名称
     * @throws 无
     * @author jack
     * @date 2026/05/22
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportEtlFillModeEnum modeEnum : CfgLogisticsCostImportEtlFillModeEnum.values()) {
            if (code.equals(modeEnum.getCode())) {
                return modeEnum.getName();
            }
        }
        return "";
    }
}
