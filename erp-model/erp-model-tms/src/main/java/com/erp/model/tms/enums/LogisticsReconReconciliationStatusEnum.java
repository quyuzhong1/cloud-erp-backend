package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流商对账单 - 对账状态汇总
 * toBeConfirm   : 待确认（沿用 ReconciliationStatusEnum.TO_BE_CONFIRM）
 * partialConfirm: 部分确认
 * confirmed     : 已确认（沿用 ReconciliationStatusEnum.CONFIRMED）
 *
 * @author Will
 * @since 2026-06-02
 */
public enum LogisticsReconReconciliationStatusEnum implements EnumMessage {

    TO_BE_CONFIRM("toBeConfirm", "待确认"),
    PARTIAL_CONFIRM("partialConfirm", "部分确认"),
    CONFIRMED("confirmed", "已确认"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsReconReconciliationStatusEnum(String code, String name) {
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
        for (LogisticsReconReconciliationStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }

    /**
     * 根据已确认数量 / 总数量推导确认状态
     * @author Will
     * @date: 2026/06/02
     * @param confirmedCount
     * @param totalCount
     * @return String
     */
    public static String resolve(int confirmedCount, int totalCount) {
        if (totalCount <= 0 || confirmedCount <= 0) {
            return TO_BE_CONFIRM.getCode();
        }
        if (confirmedCount >= totalCount) {
            return CONFIRMED.getCode();
        }
        return PARTIAL_CONFIRM.getCode();
    }
}
