package com.erp.model.mrp.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CfgRulePlatformTypeEnum implements EnumMessage {
    AMAZON("amazon", "Amazon"),
    OVERSEAS("overseas", "海外"),
    INTERNAL("internal", "国内"),
    B2B("b2b", "B2B"),
    ;

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static CfgRulePlatformTypeEnum getEnum(String code) {
        for (CfgRulePlatformTypeEnum typeEnum : CfgRulePlatformTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgRulePlatformTypeEnum statusEnum : CfgRulePlatformTypeEnum.values()) {
            if (CharSequenceUtil.equals(code,statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
