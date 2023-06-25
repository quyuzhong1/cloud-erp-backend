package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

/**
 * @author CLOUD
 * @version 1.0

 * @date 2023/3/10 9:46
 */
@NoArgsConstructor
public enum SettingEnum {

    CLEAN_JOB_DELAY_MINUTE("clean_job_delay_minute", "data_clean","清洗第三方erp数据任务延迟时间"),
    KD_TO_MB_TRANSFER_DIRECT_WAREHOUSE_CODE("kd_to_mb_transfer_direct_warehouse_code", "cross_platform_doc_conversion","金蝶直接调拨单同步库存到马帮仓库编码"),
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
