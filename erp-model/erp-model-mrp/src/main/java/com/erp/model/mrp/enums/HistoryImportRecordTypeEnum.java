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
public enum HistoryImportRecordTypeEnum implements EnumMessage {

    CFG_RULE_REPLENISHMENT("cfgRuleReplenishment", "补货规则"),
    SALES_ESTIMATE_MANUAL("salesEstimateManual", "运营月销预估"),
    DELIVERY_SUGGESTION_CONFIRM("delivery_suggestion_confirm", "发货备货确认表"),
    PURCHASE_SUGGESTION_CONFIRM("purchase_suggestion_confirm", "采购备货确认表"),

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

    HistoryImportRecordTypeEnum(String code, String name) {
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
        for (HistoryImportRecordTypeEnum statusEnum : HistoryImportRecordTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
