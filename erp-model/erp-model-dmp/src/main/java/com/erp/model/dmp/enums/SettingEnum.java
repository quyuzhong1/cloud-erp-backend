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

    CLEAN_JOB_DELAY_MINUTE("clean_job_delay_minute", SettingEnum.DATA_CLEAN,"清洗第三方erp数据任务延迟时间"),
    KD_TO_MB_TRANSFER_DIRECT_WAREHOUSE_CODE("kd_to_mb_transfer_direct_warehouse_code", SettingEnum.CROSS_PLATFORM_DOC_CONVERSION,"金蝶直接调拨单同步库存到马帮仓库编码"),
    ERP_TO_MB_WAREHOUSE_NAME("erp_to_mb_warehouse_name", SettingEnum.ERP_TO_MB_DIRECT_TRANSFER,"ERP直接调拨单同步到马帮，仓库名称"),
    ERP_TO_MB_EMPLOYEE_NAME("erp_to_mb_employee_name", SettingEnum.ERP_TO_MB_DIRECT_TRANSFER,"ERP直接调拨单推送马帮仓库，员工名称"),
    KD_TO_ERP_DELIVERY_FILTER_PLATFORM_TYPE_CODE("kd_to_erp_delivery_bill_type",SettingEnum.KD_TO_ERP_FILTER,"金蝶发货单同步到ERP过滤的平台类型编码"),
    KD_TO_ERP_DELIVERY_FILTER_BILL_TYPE("kd_to_erp_delivery_bill_type",SettingEnum.KD_TO_ERP_FILTER,"金蝶发货单同步到ERP过滤的单据类型"),
    KD_TO_ERP_B2C_SO_OUTSTOCK_BILL_TYPE("kd_to_erp_b2c_so_outstock_bill_type",SettingEnum.KD_TO_ERP_FILTER,"金蝶B2C销售出库单同步到ERP过滤的平台类型编码"),
    ;

    @EnumValue
    @JsonValue
    private String key;

    private String type;

    private String value;
    /**
     * 数据清洗类型
     */
    public static final String DATA_CLEAN = "data_clean";

    /**
     * 跨平台单据转换类型
     */
    public static final String CROSS_PLATFORM_DOC_CONVERSION = "cross_platform_doc_conversion";
    /**
     * ERP直接调拨单同步到马帮
     */
    public static final String ERP_TO_MB_DIRECT_TRANSFER = "erp_to_mb_direct_transfer";
    /**
     * 金蝶发货单同步到ERP过滤参数
     */
    public static final String KD_TO_ERP_FILTER = "kd_to_erp_filter";

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
