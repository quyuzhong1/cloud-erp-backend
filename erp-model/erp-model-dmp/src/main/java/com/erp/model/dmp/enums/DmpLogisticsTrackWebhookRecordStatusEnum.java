package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum DmpLogisticsTrackWebhookRecordStatusEnum implements EnumMessage {

    WAIT("wait", "待清洗"),
    ING("ing", "清洗中"),
    FINISH("finish", "已完成清洗"),
    ERROR("error", "失败"),
    SKIP("skip", "已跳过");

    @EnumValue
    @JsonValue
    private String code;

    private String name;

    DmpLogisticsTrackWebhookRecordStatusEnum(String code, String name) {
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
        for (DmpLogisticsTrackWebhookRecordStatusEnum statusEnum : DmpLogisticsTrackWebhookRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
