package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * B2B三方发货单装箱标签尺寸
 * goodCangCode 对应谷仓 B2B 货件标签尺寸枚举值，新增尺寸时需按谷仓文档补充唯一映射。
 */
public enum B2bPackingLabelSizeEnum implements EnumMessage {

    SIZE_100_150("100*150", "100*150", 1),
    ;

    private final String code;
    private final String name;
    private final Integer goodCangCode;

    B2bPackingLabelSizeEnum(String code, String name, Integer goodCangCode) {
        this.code = code;
        this.name = name;
        this.goodCangCode = goodCangCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getGoodCangCode() {
        return goodCangCode;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return true;
        }
        for (B2bPackingLabelSizeEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static Integer getGoodCangCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        // 当前标签尺寸枚举很少，线性查找更直观；新增大量尺寸时再引入静态 Map。
        for (B2bPackingLabelSizeEnum value : values()) {
            if (value.code.equals(code) || String.valueOf(value.goodCangCode).equals(code)) {
                return value.goodCangCode;
            }
        }
        return null;
    }
}
