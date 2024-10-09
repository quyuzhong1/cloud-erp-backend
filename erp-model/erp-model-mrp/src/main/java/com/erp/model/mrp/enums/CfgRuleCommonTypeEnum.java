package com.erp.model.mrp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 仓库配置类型枚举
 * @author will
 * @date 2024/8/24 15:19
 */
public enum CfgRuleCommonTypeEnum implements EnumMessage {

    INVENTORY("inventory", "库存配置"),
    SUGGEST("suggest", "建议配置"),
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

    CfgRuleCommonTypeEnum(String code, String name) {
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
        for (CfgRuleCommonTypeEnum statusEnum : CfgRuleCommonTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }


    public static String getBaseInventoryRedisKey(String platformType) {
        return String.join(":", "MRP", INVENTORY.getCode(), platformType);
    }

    public static String getBaseSuggestRedisKey(String platformType) {
        return String.join(":", "MRP", SUGGEST.getCode(), platformType);
    }
}
