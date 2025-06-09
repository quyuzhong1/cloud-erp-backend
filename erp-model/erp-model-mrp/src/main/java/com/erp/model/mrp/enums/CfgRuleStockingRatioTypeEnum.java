package com.erp.model.mrp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 备货系数 枚举
 * </p>
 *
 * @author will
 * @since 2024-08-23 15:05:16
 */
public enum CfgRuleStockingRatioTypeEnum implements EnumMessage {

    CONVENTIONAL("conventional", "常规品"),
    NEW("new", "新品"),
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

    CfgRuleStockingRatioTypeEnum(String code, String name) {
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
        for (CfgRuleStockingRatioTypeEnum statusEnum : CfgRuleStockingRatioTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
