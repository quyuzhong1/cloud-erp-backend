package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author CLOUD
 * @version 1.0

 * @date 2023/3/10 9:46
 */
@Getter
@NoArgsConstructor
public enum SettingEnum {

    //PLM
    PLM_PRODUCT_CERTIFICATE_IMPORT_URL("plm_product_certificate_import_url", SettingEnum.URL_CHANGE,"导入url变更"),
    PLM_NAS_USERNAME_PWD("plm_nas_username_pwd", SettingEnum.NAS_USERNAME_PWD,"nas账号名称密码"),


    CLEAN_JOB_DELAY_MINUTE("clean_job_delay_minute", SettingEnum.DATA_CLEAN,"清洗第三方erp数据任务延迟时间"),
    KD_TO_MB_TRANSFER_DIRECT_WAREHOUSE_CODE("kd_to_mb_transfer_direct_warehouse_code", SettingEnum.CROSS_PLATFORM_DOC_CONVERSION,"金蝶直接调拨单同步库存到马帮仓库编码"),
    ERP_TO_MB_WAREHOUSE_NAME("erp_to_mb_warehouse_name", SettingEnum.ERP_TO_MB_DIRECT_TRANSFER,"ERP直接调拨单同步到马帮，仓库编码"),
    ERP_TO_MB_EMPLOYEE_NAME("erp_to_mb_employee_name", SettingEnum.ERP_TO_MB_DIRECT_TRANSFER,"ERP直接调拨单推送马帮仓库，员工名称"),
    KD_TO_ERP_DELIVERY_FILTER_PLATFORM_TYPE_CODE("kd_to_erp_delivery_bill_type_code",SettingEnum.KD_TO_ERP_FILTER,"金蝶发货单同步到ERP过滤的平台类型编码"),
    KD_TO_ERP_DELIVERY_FILTER_BILL_TYPE("kd_to_erp_delivery_bill_type",SettingEnum.KD_TO_ERP_FILTER,"金蝶发货单同步到ERP过滤的单据类型"),
    KD_TO_ERP_B2C_SO_OUTSTOCK_BILL_TYPE("kd_to_erp_b2c_so_outstock_bill_type",SettingEnum.KD_TO_ERP_FILTER,"金蝶B2C销售出库单同步到ERP过滤的平台类型编码"),

    // 亚马逊SP-API配置key
    AMAZON_SP_API_USER("amazon_sp_api_user", SettingEnum.AMAZON_SP_API_CONFIG, "亚马逊SP-API用户"),
    AMAZON_SP_API_ACCESS_KEY_ID("amazon_sp_api_access_key_id", SettingEnum.AMAZON_SP_API_CONFIG, "亚马逊SP-AP用户key"),
    AMAZON_SP_API_SECRET_KEY("amazon_sp_api_secret_key", SettingEnum.AMAZON_SP_API_CONFIG, "亚马逊SP-API用户密钥"),
    AMAZON_SP_API_ROLE_ARN("amazon_sp_api_role_arn", SettingEnum.AMAZON_SP_API_CONFIG, "亚马逊SP-API用户角色权限"),

    // WMS海外入库单默认发货信息
    WMS_OVERSEAS_INBOUND_PROVINCE_NAME("province_name",SettingEnum.WMS_OVERSEAS_INBOUND, "省/州名"),
    WMS_OVERSEAS_INBOUND_CITY_NAME("city_name",SettingEnum.WMS_OVERSEAS_INBOUND, "城市名"),
    WMS_OVERSEAS_INBOUND_DISTRICT_NAME("district_name",SettingEnum.WMS_OVERSEAS_INBOUND, "区名"),
    WMS_OVERSEAS_INBOUND_FIRST_NAME("firstname",SettingEnum.WMS_OVERSEAS_INBOUND, "联系人姓"),
    WMS_OVERSEAS_INBOUND_LAST_NAME("last_name",SettingEnum.WMS_OVERSEAS_INBOUND, "联系人名"),
    WMS_OVERSEAS_INBOUND_MOBILE("mobile",SettingEnum.WMS_OVERSEAS_INBOUND, "手机号"),
    WMS_OVERSEAS_INBOUND_STREET("street",SettingEnum.WMS_OVERSEAS_INBOUND, "发货地址"),
    WMS_OVERSEAS_INBOUND_COUNTRY_CODE("country_code",SettingEnum.WMS_OVERSEAS_INBOUND, "国家代号"),

    // 任务相关
    PLATFORM_API_TASK_DELAY_SECOND("platform_api_task_delay_second", SettingEnum.CFG_TASK, "platform_api_task数据任务请求下次延迟秒数:next_time-秒数(格式json:{api_code}:{延迟秒数})"),
    AMZ_REPORT_SCHEDULE_DELAY_SECOND("amz_report_schedule_delay_second", SettingEnum.CFG_TASK, "亚马逊数据任务请求下次延迟秒数:next_time-秒数(格式json:{report_type}:{延迟秒数})"),

    //同步ERP的b2c订单同步到中台启动时间
    ERP_B2C_TO_DMP_LISTING_DATE("listing_date",SettingEnum.ERP_B2C_TO_DMP, "同步ERP的b2c订单同步到中台启动时间"),

    // 亚马逊报告配置
    AMAZON_REPORT_CHECK_COUNT("amazon_report_check_count", SettingEnum.AMAZON_REPORT, "亚马逊报告创建失败检查历史成功报告的次数,默认2"),
    AMAZON_REPORT_STOP_COUNT("amazon_report_stop_count", SettingEnum.AMAZON_REPORT, "亚马逊报告创建失败停止次数,默认3"),

    // 亚马逊货件白名单
    AMAZON_FBA_SHIPMENT_SKIP_LIST("amazon_fba_shipment_skip", SettingEnum.AMAZON_FBA_SHIPMENT_SKIP, "亚马逊FBA货件暂时跳过亚马逊请求列表(英文逗号拼接)"),

    NEW_DMP_PUSH_SWTICH_LIST("push_swtich", SettingEnum.NEW_DMP_PUSH_SWTICH, "新中台推送开关"),
    NEW_DMP_PULL_SWITCH_LIST("pull_switch", SettingEnum.NEW_DMP_PUSH_SWTICH, "新中台拉取开关"),

    //支持推送仓位的金蝶仓库
    PUSH_KINGDEE_WAREHOUSE_LOCATION_LIST("push_kingdee_warehouse_location",SettingEnum.PUSH_KINGDEE_WAREHOUSE_LOCATION,"支持推送仓位的金蝶仓库"),
    ;

    @EnumValue
    @JsonValue
    private String key;

    private String type;

    private String value;

    /**
     * url变更
     */
    public static final String URL_CHANGE = "url_change";

    /**
     * nas
     */
    public static final String NAS_USERNAME_PWD = "nas_username_pwd";

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

    /**
     * 亚马逊SP API参数
     */
    public static final String AMAZON_SP_API_CONFIG = "amazon_sp_api_config";

    /**
     * WMS海外入库单默认发货信息
     */
    public static final String WMS_OVERSEAS_INBOUND = "wms_overseas_inbound";

    /**
     * 同步erp的b2c订单到dmp中台
     */
    public static final String ERP_B2C_TO_DMP = "erp_b2c_to_dmp";

    /**
     * 亚马逊FBA货件暂时跳过亚马逊请求
     */
    public static final String AMAZON_FBA_SHIPMENT_SKIP = "amazon_fba_shipment_skip";

    /**
     * 新中台推送开关
     */
    public static final String NEW_DMP_PUSH_SWTICH = "new_dmp_push_swtich";

    /**
     * 新中台拉取开关
     */
    public static final String NEW_DMP_PULL_SWITCH = "new_dmp_push_switch";

    /**
     * 亚马逊报告
     */
    public static final String AMAZON_REPORT = "amazon_report";

    /**
     * 任务相关包括platform_api_task和amz_report_schedule
     */
    public static final String CFG_TASK = "cfg_task";

    /**
     * 支持推送仓位的金蝶仓库
     */
    public static final String PUSH_KINGDEE_WAREHOUSE_LOCATION = "push_kingdee_warehouse_location";



    SettingEnum(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }
}
