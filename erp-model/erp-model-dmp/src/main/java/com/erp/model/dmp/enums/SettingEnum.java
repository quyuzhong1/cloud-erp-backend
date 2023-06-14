package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

/**
 * @author CLOUD
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 9:46
 */
@NoArgsConstructor
public enum SettingEnum {

    CLEAN_JOB_DELAY_MINUTE("clean_job_delay_minute", "data_clean","数据清理延迟时间"),
    ;

    @EnumValue
    @JsonValue
    private String key;

    private String type;

    private String value;

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    SettingEnum(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }
}
