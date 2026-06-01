package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流商对账单关联 - 匹配类型
 * auto    : 系统自动匹配（按 trackNo / transportNo / soCode 等键命中已有物流单）
 * manual  : 手动匹配（用户在前端勾选指定物流单）
 * newBill : 新建物流单匹配（用户在前端发起"新增物流单"后绑定）
 *
 * @author Will
 * @since 2026-05-29
 */
public enum LogisticsReconRefMatchTypeEnum implements EnumMessage {

    AUTO("auto", "自动匹配"),
    MANUAL("manual", "手动匹配"),
    NEW_BILL("newBill", "新建物流单匹配"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsReconRefMatchTypeEnum(String code, String name) {
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
        for (LogisticsReconRefMatchTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }
}
