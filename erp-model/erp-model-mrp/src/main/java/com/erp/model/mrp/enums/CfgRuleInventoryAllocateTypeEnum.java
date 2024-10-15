package com.erp.model.mrp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 库存分配类型枚举
 * @author will
 * @date 2024/8/24 15:19
 */
public enum CfgRuleInventoryAllocateTypeEnum implements EnumMessage {

    SHARE("share", "共用"),
    AUTO_ALLOCATION("autoAllocation", "自动分配"),
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

    CfgRuleInventoryAllocateTypeEnum(String code, String name) {
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
        for (CfgRuleInventoryAllocateTypeEnum statusEnum : CfgRuleInventoryAllocateTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
