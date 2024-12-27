package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 对账状态枚举
 * @date 2023/11/13 15:38
 */
public enum ReconciliationStatusEnum implements EnumMessage {


    TO_BE_GENERATED("toBeGenerated","待生成"),
    TO_BE_CONFIRM("toBeConfirm","待确认"),
    CONFIRMED("confirmed","账单确认"),
    RECONCILED("Reconciled","已对账"),
    INVALID("invalid","已作废"),
    DIFF_CONFIRM("diffConfirm", "差异确认"),
    ESTIMATE_CONFIRM("estimateConfirm", "暂估确认"),
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

    ReconciliationStatusEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ReconciliationStatusEnum typeEnums : ReconciliationStatusEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
