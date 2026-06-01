package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流商对账明细（行级） - 匹配状态
 * unmatched: 未匹配
 * matching : 匹配中（异步合并匹配任务进行时占位，避免重复触发）
 * matched  : 已匹配
 * failed   : 匹配失败
 *
 * @author Will
 * @since 2026-05-29
 */
public enum LogisticsReconDetailMatchStatusEnum implements EnumMessage {

    UNMATCHED("unmatched", "未匹配"),
    MATCHING("matching", "匹配中"),
    MATCHED("matched", "已匹配"),
    FAILED("failed", "匹配失败"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsReconDetailMatchStatusEnum(String code, String name) {
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
        for (LogisticsReconDetailMatchStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(values()).map(LogisticsReconDetailMatchStatusEnum::getCode).collect(Collectors.toList());
    }
}
