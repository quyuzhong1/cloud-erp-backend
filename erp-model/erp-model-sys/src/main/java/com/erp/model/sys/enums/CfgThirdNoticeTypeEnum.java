package com.erp.model.sys.enums;

import com.common.core.constant.EnumMessage;

/**
 * <p>
 * 三方通知配置 通知类型 枚举
 * 该枚举仅用于向前端提供标准下拉选项，不做后端存储约束，
 * 存库值为枚举 name（中文），同时兼容历史自定义文本数据。
 * </p>
 *
 * @author jack
 * @since 2026-06-09
 */
public enum CfgThirdNoticeTypeEnum implements EnumMessage {
    WARN("warn", "预警通知"),
    TRADE("trade", "交易通知"),
    MARKETING("marketing", "营销通知"),
    INTERACTION("interaction", "互动通知"),
    VERIFY("verify", "验证通知"),
    SYSTEM_FUNCTION("systemFunction", "系统/功能通知"),
    SCHEDULE("schedule", "日程/提醒"),
    OTHER("other", "其他"),
    ;

    private final String code;

    private final String name;

    CfgThirdNoticeTypeEnum(String code, String name) {
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
}
