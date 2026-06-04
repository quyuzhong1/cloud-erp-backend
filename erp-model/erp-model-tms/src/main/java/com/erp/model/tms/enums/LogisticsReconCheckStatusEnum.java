package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流商对账单 - 校验状态（已合并原导入状态，单字段三态流转）
 * importing: 导入中（异步导入任务进行时）
 * pending  : 待确认（导入完成默认，原"待校验"）
 * confirmed: 已确认（人工核对通过，允许进入合并&匹配，原"已校验"）
 * 流程：导入中 → 待确认 → 已确认
 *
 * @author Will
 * @since 2026-05-29
 */
public enum LogisticsReconCheckStatusEnum implements EnumMessage {

    IMPORTING("importing", "导入中"),
    PENDING("pending", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsReconCheckStatusEnum(String code, String name) {
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
        for (LogisticsReconCheckStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(values()).map(LogisticsReconCheckStatusEnum::getCode).collect(Collectors.toList());
    }
}
