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
public enum TmsB2cDeclareReconciliationStatusEnum implements EnumMessage {

    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRM("confirm", "已确认"),
    DIFF_CONFIRM("diffConfirm", "差异确认")
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


    TmsB2cDeclareReconciliationStatusEnum(String code, String name) {
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
        for (TmsB2cDeclareReconciliationStatusEnum statusEnum : TmsB2cDeclareReconciliationStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}


