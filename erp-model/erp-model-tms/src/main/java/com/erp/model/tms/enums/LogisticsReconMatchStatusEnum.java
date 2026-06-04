package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流商对账单（主表） - 匹配状态汇总
 * unmatched: 未匹配（total_count > 0 && match_count = 0）
 * partial  : 部分匹配（0 < match_count < total_count）
 * matched  : 全部匹配（match_count >= total_count）
 *
 * @author Will
 * @since 2026-05-29
 */
public enum LogisticsReconMatchStatusEnum implements EnumMessage {

    UNMATCHED("unmatched", "未匹配"),
    PARTIAL("partial", "部分匹配"),
    MATCHED("matched", "全部匹配"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsReconMatchStatusEnum(String code, String name) {
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
        for (LogisticsReconMatchStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }

    /**
     * 根据已匹配数量 / 总数量 推导主表 match_status
     */
    public static String resolve(int matchCount, int totalCount) {
        if (totalCount <= 0 || matchCount <= 0) {
            return UNMATCHED.getCode();
        }
        if (matchCount >= totalCount) {
            return MATCHED.getCode();
        }
        return PARTIAL.getCode();
    }

    public static List<String> getStatusList() {
        return Arrays.stream(values()).map(LogisticsReconMatchStatusEnum::getCode).collect(Collectors.toList());
    }
}
