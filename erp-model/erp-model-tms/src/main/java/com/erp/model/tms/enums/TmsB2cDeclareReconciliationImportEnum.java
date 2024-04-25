package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: 报关对账
 * @author Will
 * @date: 2024/3/26 15:28
 */
public enum TmsB2cDeclareReconciliationImportEnum implements EnumMessage {

    STANDARD("standard", "标准"),
    CONFIG("config", "配置")
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


    TmsB2cDeclareReconciliationImportEnum(String code, String name) {
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
        for (TmsB2cDeclareReconciliationImportEnum statusEnum : TmsB2cDeclareReconciliationImportEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}


