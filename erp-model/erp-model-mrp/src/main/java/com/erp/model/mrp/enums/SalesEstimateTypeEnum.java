package com.erp.model.mrp.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Getter
public enum SalesEstimateTypeEnum implements EnumMessage {

    SYSTEM("SYSTEM", "系统"),
    AI("AI", "AI"),
    CUSTOMER("CUSTOMER", "自定义导入"),
    update_Import("updateImport", "导入更新");

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

    public static SalesEstimateTypeEnum getEnum(String code) {
        for (SalesEstimateTypeEnum typeEnum : SalesEstimateTypeEnum.values()) {
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
        for (SalesEstimateTypeEnum statusEnum : SalesEstimateTypeEnum.values()) {
            if (CharSequenceUtil.equals(code, statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

}
