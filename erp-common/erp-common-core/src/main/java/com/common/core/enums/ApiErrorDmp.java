package com.common.core.enums;

/**
 * Dmp service error constants.
 */
public final class ApiErrorDmp {

    private ApiErrorDmp() {
    }

    public static final ApiError BI_TOPIC_REQUIRED = new ApiError("BI_TOPIC_REQUIRED", 7000, "专题不能为空");
    public static final ApiError BI_NOT_OWNER = new ApiError("BI_NOT_OWNER", 7001, "不能设置非自己创建的仪表盘");
    public static final ApiError BI_MODULE_NAME_EXISTS = new ApiError("BI_MODULE_NAME_EXISTS", 7002, "模块名称不能重复");
    public static final ApiError BI_MODULE_REQUIRED = new ApiError("BI_MODULE_REQUIRED", 7003, "模块不能为空");
    public static final ApiError BI_SALES_MONITOR_SETTING_REQUIRED = new ApiError("BI_SALES_MONITOR_SETTING_REQUIRED", 7004, "销售监控设置不能为空");
    public static final ApiError BI_NOT_DASHBOARD = new ApiError("BI_NOT_DASHBOARD", 7005, "不是仪表盘");
    public static final ApiError BI_SALES_MONITOR_TYPE_DUPLICATE = new ApiError("BI_SALES_MONITOR_TYPE_DUPLICATE", 7006, "销售监控类型不能重复选择");
    public static final ApiError BI_DASHBOARD_NOT_FOUND = new ApiError("BI_DASHBOARD_NOT_FOUND", 7007, "暂无仪表盘，请先创建仪表盘");
    public static final ApiError BI_AT_LEAST_ONE_LAYOUT = new ApiError("BI_AT_LEAST_ONE_LAYOUT", 7008, "至少需要一个布局");
    public static final ApiError BI_DUPLICATE_IN_PLATFORM_MODULE = new ApiError("BI_DUPLICATE_IN_PLATFORM_MODULE", 7009, "同平台、模块下不能新增相同字段");
    public static final ApiError BI_MODULE_NAME_MAX = new ApiError("BI_MODULE_NAME_MAX", 7010, "模块名称最大30字符");
    public static final ApiError BI_MODULE_DESC_MAX = new ApiError("BI_MODULE_DESC_MAX", 7011, "模块说明最大200字符");
    public static final ApiError BI_FIN_SALES_DATE_TYPE = new ApiError("BI_FIN_SALES_DATE_TYPE", 7012, "财务销售额只支持月，季，年维度查询");
    public static final ApiError BI_DATE_RANGE_THIRTY_ONE = new ApiError("BI_DATE_RANGE_THIRTY_ONE", 7013, "日范围不能大于31天");
    public static final ApiError BI_DATE_RANGE_WEEK_DAY = new ApiError("BI_DATE_RANGE_WEEK_DAY", 7014, "周范围不能大于12周");
    public static final ApiError BI_SALE_RANGE_EXIST = new ApiError("BI_SALE_RANGE_EXIST", 7015, "区间类型不能为空");
    public static final ApiError BI_SETTLE_METHOD_EXIST = new ApiError("BI_SETTLE_METHOD_EXIST", 7016, "结算方式不能为空");
    public static final ApiError DMP_KINGDEE_FIELD_NOT_FOUND = new ApiError("DMP_KINGDEE_FIELD_NOT_FOUND", 3500, "金蝶推送未配置同步字段");
    public static final ApiError DMP_KINGDEE_DATA_NOT_FOUND = new ApiError("DMP_KINGDEE_DATA_NOT_FOUND", 3501, "查询无数据，无需处理");
    public static final ApiError DMP_KINGDEE_ADD_FAILED = new ApiError("DMP_KINGDEE_ADD_FAILED", 3502, "金蝶系统新增数据失败");
    public static final ApiError DMP_KINGDEE_DETAIL_ID_NOT_FOUND = new ApiError("DMP_KINGDEE_DETAIL_ID_NOT_FOUND", 3503, "未查询到子单据id");
    public static final ApiError DMP_ADDRESS_OR_CONTACT_REQUIRED = new ApiError("DMP_ADDRESS_OR_CONTACT_REQUIRED", 3504, "地址编码或联系人编号是空，同步金蝶失败，请手动维护数据");
    public static final ApiError DMP_PARENT_ASSISTANT_NOT_FOUND = new ApiError("DMP_PARENT_ASSISTANT_NOT_FOUND", 3505, "未找到上级辅助资料");
    public static final ApiError DMP_PUSH_TASK_NOT_FOUND = new ApiError("DMP_PUSH_TASK_NOT_FOUND", 3506, "未找到中台推送任务");
    public static final ApiError DMP_THIRD_ALREADY_BINDED = new ApiError("DMP_THIRD_ALREADY_BINDED", 3507, "第三方{0}【{1}】已经被【{2}】绑定");
    public static final ApiError DMP_THIRD_SHOP_NOT_FOUND = new ApiError("DMP_THIRD_SHOP_NOT_FOUND", 3508, "第三方店铺不存在");
    public static final ApiError DMP_THIRD_WAREHOUSE_NOT_FOUND = new ApiError("DMP_THIRD_WAREHOUSE_NOT_FOUND", 3509, "第三方仓库不存在");
    public static final ApiError DMP_THIRD_SYS_TYPE_SINGLE_BINDING = new ApiError("DMP_THIRD_SYS_TYPE_SINGLE_BINDING", 3510, "同一个第三方平台只能绑定一个{0}");
    public static final ApiError DMP_THIRD_LOGISTICS_NOT_FOUND = new ApiError("DMP_THIRD_LOGISTICS_NOT_FOUND", 3511, "第三方渠道不存在");
    public static final ApiError DMP_PUSH_CFG_NOT_FOUND = new ApiError("DMP_PUSH_CFG_NOT_FOUND", 3512, "未找到推送配置项【{0}】");
    public static final ApiError DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_FOUND = new ApiError("DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_FOUND", 3513, "出库同步差异记录不存在");
    public static final ApiError DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_PLATFORM = new ApiError("DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_PLATFORM", 3514, "请选择差异标签为平台单据多的");
    public static final ApiError DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PERIOD = new ApiError("DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PERIOD", 3515, "请选择同一个核算周期的数据");
    public static final ApiError DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PLATFORM = new ApiError("DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PLATFORM", 3516, "请选择同一个平台的数据");
    public static final ApiError DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_NOT_ALLOW_NULL = new ApiError("DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_NOT_ALLOW_NULL", 3513, "仓库操作类型不允许为空");
    public static final ApiError DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_MISSING_ENUM = new ApiError("DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_MISSING_ENUM", 3514, "缺少必要的仓库操作类型【{0}】");
    public static final ApiError DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_VALUE_EMPTY = new ApiError("DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_VALUE_EMPTY", 3515, "仓库操作类型或描述不能为空");
    public static final ApiError DMP_KINGDEE_SUBORDER_NOT_ALLOW_DISAPPROVE = new ApiError("DMP_KINGDEE_SUBORDER_NOT_ALLOW_DISAPPROVE", 3516, "请操作金蝶反审核至待提交后执行反审核");
    public static final ApiError DMP_KINGDEE_SUBCONTRACT_BOM_PARENT_MATCH_AMBIGUOUS = new ApiError("DMP_KINGDEE_SUBCONTRACT_BOM_PARENT_MATCH_AMBIGUOUS", 3517, "委外用料清单变更单{0}按分录行号无法唯一匹配父行，候选父行数={1}");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_PARAM_SIGN_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_PARAM_SIGN_REQUIRED", 3526, "快递100回调缺少param或sign");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_SIGN_INVALID = new ApiError("DMP_KUAIDI100_WEBHOOK_SIGN_INVALID", 3527, "快递100回调验签失败");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_RAW_DATA_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_RAW_DATA_REQUIRED", 3528, "快递100 webhook 原始数据不能为空");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_RAW_DATA_JSON_INVALID = new ApiError("DMP_KUAIDI100_WEBHOOK_RAW_DATA_JSON_INVALID", 3529, "快递100 webhook 原始数据不是合法JSON");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_RAW_DATA_PARAM_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_RAW_DATA_PARAM_REQUIRED", 3530, "快递100 webhook 原始数据缺少param");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_PARAM_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_PARAM_REQUIRED", 3531, "快递100 webhook param不能为空");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_PARAM_JSON_INVALID = new ApiError("DMP_KUAIDI100_WEBHOOK_PARAM_JSON_INVALID", 3532, "快递100 webhook param不是合法JSON");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_LAST_RESULT_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_LAST_RESULT_REQUIRED", 3533, "快递100 webhook 缺少lastResult");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_TRACK_NO_REQUIRED = new ApiError("DMP_KUAIDI100_WEBHOOK_TRACK_NO_REQUIRED", 3534, "快递100 webhook 缺少物流单号");
    public static final ApiError DMP_KUAIDI100_WEBHOOK_FORM_URL_DECODE_FAILED = new ApiError("DMP_KUAIDI100_WEBHOOK_FORM_URL_DECODE_FAILED", 3535, "快递100 webhook 表单参数URL解码失败");
    public static final ApiError DMP_TRACK123_WEBHOOK_RAW_DATA_REQUIRED = new ApiError("DMP_TRACK123_WEBHOOK_RAW_DATA_REQUIRED", 3536, "Track123 webhook 原始数据不能为空");
    public static final ApiError DMP_TRACK123_WEBHOOK_RAW_DATA_JSON_INVALID = new ApiError("DMP_TRACK123_WEBHOOK_RAW_DATA_JSON_INVALID", 3537, "Track123 webhook 原始数据不是合法JSON");
    public static final ApiError DMP_TRACK123_WEBHOOK_DATA_NODE_INVALID = new ApiError("DMP_TRACK123_WEBHOOK_DATA_NODE_INVALID", 3538, "Track123 webhook 原始数据data节点不是合法JSON对象");
    public static final ApiError DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED = new ApiError("DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED", 3539, "Track123 webhook 原始数据缺少trackNo");
    public static final ApiError MAPPING_FIELD_VALUE_REQUIRED = new ApiError("MAPPING_FIELD_VALUE_REQUIRED", 7500, "字段值对应关系不能为空");
    public static final ApiError MAPPING_EN_DESC_DUPLICATE = new ApiError("MAPPING_EN_DESC_DUPLICATE", 7501, "同平台下存在相同英文描述");
    public static final ApiError MAPPING_NOT_SET_PUSH_FORBIDDEN = new ApiError("MAPPING_NOT_SET_PUSH_FORBIDDEN", 7502, "未设置字段映射，不支持推送");
    public static final ApiError MAPPING_SKU_MAPPING_EXIST = new ApiError("MAPPING_SKU_MAPPING_EXIST", 7503, "该平台SKU已存在SKU映射关系!");
    public static final ApiError MAPPING_SKU_MAPPING_NOT_EXIST = new ApiError("MAPPING_SKU_MAPPING_NOT_EXIST", 7503, "该平台SKU在对照表不存在!");
    public static final ApiError MAPPING_SKU_RULE_REQUIRED = new ApiError("MAPPING_SKU_RULE_REQUIRED", 7504, "SKU匹配规则详情不能为空");
    public static final ApiError MAPPING_SKU_HISTORY_EXISTS = new ApiError("MAPPING_SKU_HISTORY_EXISTS", 7505, "当前SKU映射关系在【{0}】中已存在历史记录，不支持修改");
    public static final ApiError MAPPING_WAREHOUSE_WDT_NOT_FOUND = new ApiError("MAPPING_WAREHOUSE_WDT_NOT_FOUND", 7506, "同步旺店通B2C单据时未找到对应仓库映射【{0}】");
    public static final ApiError MAPPING_SHOP_WDT_NOT_FOUND = new ApiError("MAPPING_SHOP_WDT_NOT_FOUND", 7507, "同步旺店通B2C单据时未找到对应店铺映射【{0}】");
    public static final ApiError MAPPING_SKU_WDT_NOT_FOUND = new ApiError("MAPPING_SKU_WDT_NOT_FOUND", 7508, "同步旺店通单据时未找到对应SKU【{0}】");
    public static final ApiError MAPPING_THIRD_SHOP_EXISTS = new ApiError("MAPPING_THIRD_SHOP_EXISTS", 7509, "店铺【{0}】已存在第三方映射关系，请在【中台配置】页面中解除绑定后再进行操作!");
    public static final ApiError MAPPING_START_DATE_INVALID = new ApiError("MAPPING_START_DATE_INVALID", 7510, "启用日期不能早于上个映射关系的开始时间【{0}】");
    public static final ApiError MAPPING_MSKU_NOT_MAPPING = new ApiError("MAPPING_MSKU_NOT_MAPPING", 7511, "MSKU【{0}】未映射SKU");
    public static final ApiError MAPPING_MSKU_NOT_EXIST = new ApiError("MAPPING_MSKU_NOT_EXIST", 7512, "MSKU不存在");

    static ApiError[] values() {
        return new ApiError[]{
                BI_TOPIC_REQUIRED,
                BI_NOT_OWNER,
                BI_MODULE_NAME_EXISTS,
                BI_MODULE_REQUIRED,
                BI_SALES_MONITOR_SETTING_REQUIRED,
                BI_NOT_DASHBOARD,
                BI_SALES_MONITOR_TYPE_DUPLICATE,
                BI_DASHBOARD_NOT_FOUND,
                BI_AT_LEAST_ONE_LAYOUT,
                BI_DUPLICATE_IN_PLATFORM_MODULE,
                BI_MODULE_NAME_MAX,
                BI_MODULE_DESC_MAX,
                BI_FIN_SALES_DATE_TYPE,
                BI_DATE_RANGE_THIRTY_ONE,
                BI_DATE_RANGE_WEEK_DAY,
                BI_SALE_RANGE_EXIST,
                BI_SETTLE_METHOD_EXIST,
                DMP_KINGDEE_FIELD_NOT_FOUND,
                DMP_KINGDEE_DATA_NOT_FOUND,
                DMP_KINGDEE_ADD_FAILED,
                DMP_KINGDEE_DETAIL_ID_NOT_FOUND,
                DMP_ADDRESS_OR_CONTACT_REQUIRED,
                DMP_PARENT_ASSISTANT_NOT_FOUND,
                DMP_PUSH_TASK_NOT_FOUND,
                DMP_THIRD_ALREADY_BINDED,
                DMP_THIRD_SHOP_NOT_FOUND,
                DMP_THIRD_WAREHOUSE_NOT_FOUND,
                DMP_THIRD_SYS_TYPE_SINGLE_BINDING,
                DMP_THIRD_LOGISTICS_NOT_FOUND,
                DMP_PUSH_CFG_NOT_FOUND,
                DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_FOUND,
                DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_PLATFORM,
                DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PERIOD,
                DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PLATFORM,
                DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_NOT_ALLOW_NULL,
                DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_MISSING_ENUM,
                DMP_THIRD_WAREHOUSE_WAREHOUSE_OPERATION_VALUE_EMPTY,
                DMP_KINGDEE_SUBORDER_NOT_ALLOW_DISAPPROVE,
                DMP_KINGDEE_SUBCONTRACT_BOM_PARENT_MATCH_AMBIGUOUS,
                DMP_KUAIDI100_WEBHOOK_PARAM_SIGN_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_SIGN_INVALID,
                DMP_KUAIDI100_WEBHOOK_RAW_DATA_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_RAW_DATA_JSON_INVALID,
                DMP_KUAIDI100_WEBHOOK_RAW_DATA_PARAM_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_PARAM_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_PARAM_JSON_INVALID,
                DMP_KUAIDI100_WEBHOOK_LAST_RESULT_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_TRACK_NO_REQUIRED,
                DMP_KUAIDI100_WEBHOOK_FORM_URL_DECODE_FAILED,
                DMP_TRACK123_WEBHOOK_RAW_DATA_REQUIRED,
                DMP_TRACK123_WEBHOOK_RAW_DATA_JSON_INVALID,
                DMP_TRACK123_WEBHOOK_DATA_NODE_INVALID,
                DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED,
                MAPPING_FIELD_VALUE_REQUIRED,
                MAPPING_EN_DESC_DUPLICATE,
                MAPPING_NOT_SET_PUSH_FORBIDDEN,
                MAPPING_SKU_MAPPING_EXIST,
                MAPPING_SKU_MAPPING_NOT_EXIST,
                MAPPING_SKU_RULE_REQUIRED,
                MAPPING_SKU_HISTORY_EXISTS,
                MAPPING_WAREHOUSE_WDT_NOT_FOUND,
                MAPPING_SHOP_WDT_NOT_FOUND,
                MAPPING_SKU_WDT_NOT_FOUND,
                MAPPING_THIRD_SHOP_EXISTS,
                MAPPING_START_DATE_INVALID,
                MAPPING_MSKU_NOT_MAPPING,
                MAPPING_MSKU_NOT_EXIST,
        };
    }
}
