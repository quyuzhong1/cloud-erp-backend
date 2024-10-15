package com.erp.model.mrp.enums;

import cn.hutool.core.util.StrUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CfgRuleStockingModeEnum implements EnumMessage {
    LOCAL_TO_FBA("localToFba", "FBA备货（本地发FBA）"),
    LOCAL_TO_OVERSEAS("localToOverseas", "海外备货（本地海外仓）"),
    LOCAL("local", "本地备货"),
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

    public static CfgRuleStockingModeEnum getEnum(String code) {
        for (CfgRuleStockingModeEnum typeEnum : CfgRuleStockingModeEnum.values()) {
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
        for (CfgRuleStockingModeEnum statusEnum : CfgRuleStockingModeEnum.values()) {
            if (StrUtil.equals(code,statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
