package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 费用项配置 识别维度 枚举
 *
 * @author jack
 * @since 2026-06-14
 */
public enum CfgLogisticsCostImportIdentifyTypeEnum implements EnumMessage {
    IDENTIFY_NO("identify_no", "按识别单号"),
    IDENTIFY_NO_SUPPLIER("identify_no_supplier", "按识别单号+物流商"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    CfgLogisticsCostImportIdentifyTypeEnum(String code, String name) {
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

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgLogisticsCostImportIdentifyTypeEnum item : CfgLogisticsCostImportIdentifyTypeEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportIdentifyTypeEnum item : CfgLogisticsCostImportIdentifyTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static boolean isValidCode(String code) {
        return StringUtils.isNotBlank(getName(code));
    }
}
