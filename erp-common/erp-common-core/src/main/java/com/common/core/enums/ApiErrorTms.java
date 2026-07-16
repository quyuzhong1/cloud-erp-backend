package com.common.core.enums;

/**
 * Tms service error constants.
 */
public final class ApiErrorTms {

    private ApiErrorTms() {
    }

    public static final ApiError FIRST_MILE_FBA_SHIPMENT_NOT_EXIST_BILL = new ApiError("FIRST_MILE_FBA_SHIPMENT_NOT_EXIST_BILL", 11500, "货件单据不存在！");
    public static final ApiError FIRST_MILE_SHIPMENT_NOT_FOUND = new ApiError("FIRST_MILE_SHIPMENT_NOT_FOUND", 11501, "未找到头程发货单");
    public static final ApiError FIRST_MILE_SHIPMENT_DELETE_ALLOWED_PENDING_ONLY = new ApiError("FIRST_MILE_SHIPMENT_DELETE_ALLOWED_PENDING_ONLY", 11502, "只有未发货的头程数据支持删除");
    public static final ApiError FIRST_MILE_SHIPMENT_DETAIL_NOT_EXIST = new ApiError("FIRST_MILE_SHIPMENT_DETAIL_NOT_EXIST", 11503, "货件详情不存在");
    public static final ApiError FIRST_MILE_SHIPMENT_NOT_EXIST = new ApiError("FIRST_MILE_SHIPMENT_NOT_EXIST", 11504, "货件不存在");
    public static final ApiError FIRST_MILE_SHIPMENT_SKU_NOT_MAPPED = new ApiError("FIRST_MILE_SHIPMENT_SKU_NOT_MAPPED", 11505, "【{0}】包含未匹配到SKU的货件，不允许下推发货单");
    public static final ApiError FIRST_MILE_SHIPMENT_CONTAIN_COMBINATION_REQUIRE_MACHINE = new ApiError("FIRST_MILE_SHIPMENT_CONTAIN_COMBINATION_REQUIRE_MACHINE", 11506, "发货单【{0}】包含组合产品，请先下推加工单并审核通过后重试");
    public static final ApiError FIRST_MILE_SHIPMENT_INVENTORY_INSUFFICIENT = new ApiError("FIRST_MILE_SHIPMENT_INVENTORY_INSUFFICIENT", 11507, "提示：SKU【{0}】发货仓【{1}】冻结库存不足，无法审核该发货单");
    public static final ApiError FIRST_MILE_SHIPMENT_RECEIVE_EXIST_REVERSE_FORBIDDEN = new ApiError("FIRST_MILE_SHIPMENT_RECEIVE_EXIST_REVERSE_FORBIDDEN", 11508, "已存在货件签收数量的发货单，不允许反审核!");
    public static final ApiError FIRST_MILE_SHIPMENT_STATUS_FINISH_ONLY = new ApiError("FIRST_MILE_SHIPMENT_STATUS_FINISH_ONLY", 11509, "仅【已发货】或【自动完结】状态的货件允许手动完结!");
    public static final ApiError FIRST_MILE_SHIPMENT_ALREADY_PUSHED_NOT_DELETE = new ApiError("FIRST_MILE_SHIPMENT_ALREADY_PUSHED_NOT_DELETE", 11510, "已下推发货单，不能删除!");
    public static final ApiError FIRST_MILE_SHIPMENT_STATUS_CHECK_NOT_DELETE = new ApiError("FIRST_MILE_SHIPMENT_STATUS_CHECK_NOT_DELETE", 11511, "状态为 DELETED 或 CANCELLED 的货件不允许下推发货单");
    public static final ApiError FIRST_MILE_SHIPMENT_ERROR = new ApiError("FIRST_MILE_SHIPMENT_ERROR", 11512, "货件不存在或状态异常");
    public static final ApiError FIRST_MILE_SHIPMENT_PLAN_NOT_EXIST = new ApiError("FIRST_MILE_SHIPMENT_PLAN_NOT_EXIST", 11513, "未找到发货计划单");
    public static final ApiError FIRST_MILE_SHIPMENT_DETAIL_NOT_DISAPPROVE = new ApiError("FIRST_MILE_SHIPMENT_DETAIL_NOT_DISAPPROVE", 11514, "已下推发货单，不允许执行反审核");
    public static final ApiError FIRST_MILE_SHIPMENT_REQ_NOT_DISAPPROVE = new ApiError("FIRST_MILE_SHIPMENT_REQ_NOT_DISAPPROVE", 11515, "已下推要货申请，不允许执行反审核");
    public static final ApiError FIRST_MILE_SHIPMENT_QTY_EXCEED_DECLARE_QTY = new ApiError("FIRST_MILE_SHIPMENT_QTY_EXCEED_DECLARE_QTY", 11516, "SKU【{0}】发货数量超过申报数量，不允许下推");
    public static final ApiError FIRST_MILE_SHIPMENT_REQ_NOT_FOUND = new ApiError("FIRST_MILE_SHIPMENT_REQ_NOT_FOUND", 11517, "未找到要货申请单");
    public static final ApiError FIRST_MILE_SHIPMENT_REQ_DETAIL_NOT_FOUND = new ApiError("FIRST_MILE_SHIPMENT_REQ_DETAIL_NOT_FOUND", 11518, "未找到要货申请单明细");
    public static final ApiError FIRST_MILE_SHIPMENT_NOTICE_DETAIL_NOT_FOUND = new ApiError("FIRST_MILE_SHIPMENT_NOTICE_DETAIL_NOT_FOUND", 11519, "未找到发货通知单明细");
    public static final ApiError FIRST_MILE_SHIPMENT_WAIT_HANDLE_ONLY = new ApiError("FIRST_MILE_SHIPMENT_WAIT_HANDLE_ONLY", 11520, "待处理状态的要货单才能处理");
    public static final ApiError FIRST_MILE_SHIPMENT_HANDLE_ING_FINISH_ONLY = new ApiError("FIRST_MILE_SHIPMENT_HANDLE_ING_FINISH_ONLY", 11521, "单号【{0}】处理中状态的要货单才能完成");
    public static final ApiError FIRST_MILE_SHIPMENT_ONLY_FOR_OVERSEAS_WAREHOUSE = new ApiError("FIRST_MILE_SHIPMENT_ONLY_FOR_OVERSEAS_WAREHOUSE", 11522, "只有备货海外仓的发货单允许下推入库单");
    public static final ApiError FIRST_MILE_SHIPMENT_APPROVE_ONLY_CAN_PUSH_OVERSEAS_INBOUND = new ApiError("FIRST_MILE_SHIPMENT_APPROVE_ONLY_CAN_PUSH_OVERSEAS_INBOUND", 11523, "只有待审核的数据允许下推海外仓入库单");
    public static final ApiError FIRST_MILE_SHIPMENT_HANDLE_PRINT_PICKING_ALLOWED = new ApiError("FIRST_MILE_SHIPMENT_HANDLE_PRINT_PICKING_ALLOWED", 11524, "仅处理中或已处理状态的要货单允许打印拣货单");
    public static final ApiError FIRST_MILE_SHIPMENT_PACKING_NOT_COMPLETED_CANNOT_GENERATE_INBOUND = new ApiError("FIRST_MILE_SHIPMENT_PACKING_NOT_COMPLETED_CANNOT_GENERATE_INBOUND", 11525, "装箱未完成，不能下推入库单");
    public static final ApiError FIRST_MILE_SHIPMENT_FINANCE_COST_ALLOCATION_REVERSE_FORBIDDEN = new ApiError("FIRST_MILE_SHIPMENT_FINANCE_COST_ALLOCATION_REVERSE_FORBIDDEN", 11526, "已进行费用分摊，不允许执行反审核操作");
    public static final ApiError FIRST_MILE_SHIPMENT_WAREHOUSE_REQUIRED = new ApiError("FIRST_MILE_SHIPMENT_WAREHOUSE_REQUIRED", 11527, "头程发货单【{0}】配置的发货仓库不能为空");
    public static final ApiError FIRST_MILE_SHIPMENT_DELIVERY_GENERATE_FAIL = new ApiError("FIRST_MILE_SHIPMENT_DELIVERY_GENERATE_FAIL", 11528, "下推头程发货单失败");
    public static final ApiError FIRST_MILE_SHIPMENT_AWD_OUTSTOCK_NOT_EXIST = new ApiError("FIRST_MILE_SHIPMENT_AWD_OUTSTOCK_NOT_EXIST", 11529, "AWD出库货件不存在");
    public static final ApiError FIRST_MILE_SHIPMENT_GENERATE_NEED_BILL_DATE = new ApiError("FIRST_MILE_SHIPMENT_GENERATE_NEED_BILL_DATE", 11530, "出库货件【{0}】没有发货时间，不支持生成头程发货单");
    public static final ApiError FIRST_MILE_COST_ALLOCATION_ORG_ID_REQUIRED = new ApiError("FIRST_MILE_COST_ALLOCATION_ORG_ID_REQUIRED", 11531, "分摊组织id为空");
    public static final ApiError FIRST_MILE_SHIPMENT_DEST_COUNTRY_INCONSISTENT = new ApiError("FIRST_MILE_SHIPMENT_DEST_COUNTRY_INCONSISTENT", 11532, "所选头程发货单国家不一致");
    public static final ApiError FIRST_MILE_SHIPMENT_DEST_COUNTRY_NOT_MAINTAINED = new ApiError("FIRST_MILE_SHIPMENT_DEST_COUNTRY_NOT_MAINTAINED", 11533, "所选头程发货单未维护目的国");
    public static final ApiError FIRST_MILE_SHIPMENT_SAVE_FAILED = new ApiError("FIRST_MILE_SHIPMENT_SAVE_FAILED", 11534, "头程发货单保存失败");
    public static final ApiError FIRST_MILE_SHIPMENT_DECLARE_SOURCE_NOT_FOUND = new ApiError("FIRST_MILE_SHIPMENT_DECLARE_SOURCE_NOT_FOUND", 11535, "头程发货单【{0}】未查询到可生成报关明细的来源数据");
    public static final ApiError FIRST_MILE_SHIPMENT_PACKING_TASK_NOT_GENERATED = new ApiError("FIRST_MILE_SHIPMENT_PACKING_TASK_NOT_GENERATED", 11536, "未生成装箱任务，不允许审核");
    public static final ApiError FIRST_MILE_SHIPMENT_PACKING_WEIGHT_REQUIRED_FOR_APPROVE = new ApiError("FIRST_MILE_SHIPMENT_PACKING_WEIGHT_REQUIRED_FOR_APPROVE", 11537, "已装箱且全部称重后才能审核通过");
    public static final ApiError FIRST_MILE_SHIPMENT_AVAILABLE_INVENTORY_INSUFFICIENT = new ApiError("FIRST_MILE_SHIPMENT_AVAILABLE_INVENTORY_INSUFFICIENT", 11538, "仓库【{0}】SKU【{1}】发货数量【{2}】无足够可用库存【{3}】");
    public static final ApiError FIRST_MILE_SHIPMENT_MACHINE_EXISTS_DELETE_FORBIDDEN = new ApiError("FIRST_MILE_SHIPMENT_MACHINE_EXISTS_DELETE_FORBIDDEN", 11539, "存在关联的加工单【{0}】，头程发货单禁止删除");
    public static final ApiError FIRST_MILE_SHIPMENT_INBOUND_EXISTS_DELETE_FORBIDDEN = new ApiError("FIRST_MILE_SHIPMENT_INBOUND_EXISTS_DELETE_FORBIDDEN", 11540, "存在下游海外入库单【{0}】，禁止删除");
    public static final ApiError FIRST_MILE_SHIPMENT_THIRD_WAREHOUSE_APPROVE_PUSH_FAILED = new ApiError("FIRST_MILE_SHIPMENT_THIRD_WAREHOUSE_APPROVE_PUSH_FAILED", 11541, "推送第三方仓库【发货单审核】失败：{0}");
    public static final ApiError FIRST_MILE_SHIPMENT_LOGISTICS_AUTO_GENERATE_FAILED = new ApiError("FIRST_MILE_SHIPMENT_LOGISTICS_AUTO_GENERATE_FAILED", 11542, "头程发货单【{0}】审核后自动生成物流单失败：{1}");
    public static final ApiError FIRST_MILE_SHIPMENT_WORKFLOW_START_FAILED = new ApiError("FIRST_MILE_SHIPMENT_WORKFLOW_START_FAILED", 11543, "头程发货单启动流程失败：{0}");
    public static final ApiError FIRST_MILE_SHIPMENT_DEST_WAREHOUSE_REQUIRED = new ApiError("FIRST_MILE_SHIPMENT_DEST_WAREHOUSE_REQUIRED", 11544, "目的仓信息不能为空");
    public static final ApiError FIRST_MILE_DELIVERY_DECLARE_ALREADY_GENERATED = new ApiError("FIRST_MILE_DELIVERY_DECLARE_ALREADY_GENERATED", 11545, "发货单【{0}】已生成报关单");
    public static final ApiError LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_RECONCILIATION_MONTH = new ApiError("LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_RECONCILIATION_MONTH", 11528, "物流费用导入对账月份不能为空");
    public static final ApiError LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL = new ApiError("LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL", 11529, "平台订单号、发货单号、销售单号、物流跟踪单号必须至少填一个");
    public static final ApiError LOGISTICS_PDF_MERGE_ERROR = new ApiError("LOGISTICS_PDF_MERGE_ERROR", 13500, "打印面单/配货单失败，合并PDF时出错");
    public static final ApiError LOGISTICS_PDF_MERGE_SKU_BARCODE_ERROR = new ApiError("LOGISTICS_PDF_MERGE_SKU_BARCODE_ERROR", 13501, "打印SKU条码失败，合并PDF时出错");
    public static final ApiError LOGISTICS_PDF_SO_MERGE_ERROR = new ApiError("LOGISTICS_PDF_SO_MERGE_ERROR", 13502, "打印面单失败，合并PDF时出错");
    public static final ApiError LOGISTICS_NO_TRACKING_NUMBER_CANNOT_MANUAL_SHIP = new ApiError("LOGISTICS_NO_TRACKING_NUMBER_CANNOT_MANUAL_SHIP", 13503, "请先申请物流单号后再执行手动标发");
    public static final ApiError LOGISTICS_ALREADY_PACKAGE_TRANSFER_NOT_INTERCEPT = new ApiError("LOGISTICS_ALREADY_PACKAGE_TRANSFER_NOT_INTERCEPT", 13504, "拦截单号【{0}】已组包或已预报成功，请先取消组包/预报后再操作");
    public static final ApiError LOGISTICS_HANDLE_STATUS_ALREADY_HANDLED_OR_CANCEL_NOT = new ApiError("LOGISTICS_HANDLE_STATUS_ALREADY_HANDLED_OR_CANCEL_NOT", 13505, "处理状态为【已处理】或【已取消】的单据，不支持再次发起物流拦截");
    public static final ApiError LOGISTICS_UPLOAD_SUCCESS_NOT_DELETE = new ApiError("LOGISTICS_UPLOAD_SUCCESS_NOT_DELETE", 13506, "上传成功状态的数据不允许删除");
    public static final ApiError LOGISTICS_DELIVERY_INTERCEPT_READY_PACKAGED = new ApiError("LOGISTICS_DELIVERY_INTERCEPT_READY_PACKAGED", 13507, "销售订单号【{0}】已组包，不支持拦截操作");
    public static final ApiError LOGISTICS_PACKING_REF_ORDER_APPROVED_FORBIDDEN = new ApiError("LOGISTICS_PACKING_REF_ORDER_APPROVED_FORBIDDEN", 13508, "关联单号【{0}】已审核，不能修改装箱信息");
    public static final ApiError LOGISTICS_PACKING_TASK_NOT_FOUND = new ApiError("LOGISTICS_PACKING_TASK_NOT_FOUND", 13509, "装箱任务记录不存在");
    public static final ApiError LOGISTICS_SYNC_ADDRESS_NOT_EDITABLE = new ApiError("LOGISTICS_SYNC_ADDRESS_NOT_EDITABLE", 13510, "同步自物流商的地址不允许修改");
    public static final ApiError LOGISTICS_SYNC_ADDRESS_NOT_DELETABLE = new ApiError("LOGISTICS_SYNC_ADDRESS_NOT_DELETABLE", 13511, "同步自物流商的地址不允许删除");
    public static final ApiError LOGISTICS_PACKING_SPEC_NOT_FOUND = new ApiError("LOGISTICS_PACKING_SPEC_NOT_FOUND", 13512, "箱规记录不存在");
    public static final ApiError LOGISTICS_PACKING_RECORD_NOT_FOUND = new ApiError("LOGISTICS_PACKING_RECORD_NOT_FOUND", 13513, "装箱记录不存在");
    public static final ApiError LOGISTICS_PACKING_FNSKU_QTY_EXCEEDS_UNPACKED = new ApiError("LOGISTICS_PACKING_FNSKU_QTY_EXCEEDS_UNPACKED", 13514, "SKU【{0}】FnSKU【{1}】装箱数量不能超过未装箱数量【{2}】");
    public static final ApiError LOGISTICS_PACKING_SKU_QTY_EXCEEDS_BOX = new ApiError("LOGISTICS_PACKING_SKU_QTY_EXCEEDS_BOX", 13515, "SSKU【{0}】装箱数量不能超过本箱当前已装箱数量【{1}】");
    public static final ApiError LOGISTICS_PACKING_SKU_NOT_IN_ASSOCIATED_ORDER = new ApiError("LOGISTICS_PACKING_SKU_NOT_IN_ASSOCIATED_ORDER", 13516, "SKU【{0}】在关联单中不存在");
    public static final ApiError LOGISTICS_PACKING_SKU_NOT_IN_BOX = new ApiError("LOGISTICS_PACKING_SKU_NOT_IN_BOX", 13517, "SKU【{0}】在当前装箱记录中不存在，不能执行移出");
    public static final ApiError LOGISTICS_PACKING_ASSOCIATED_ORDER_APPROVED_EDIT_DELETE_FORBIDDEN = new ApiError("LOGISTICS_PACKING_ASSOCIATED_ORDER_APPROVED_EDIT_DELETE_FORBIDDEN", 13518, "关联单号已审核，不支持编辑或删除");
    public static final ApiError LOGISTICS_PACKING_DELIVERY_CHECK_FORBIDDEN = new ApiError("LOGISTICS_PACKING_DELIVERY_CHECK_FORBIDDEN", 13519, "发货单已审核且装箱任务状态为【已装箱且已称重】时，不支持编辑或删除");
    public static final ApiError LOGISTICS_PACKING_SKU_FNSKU_QTY_EXCEEDS_DELIVERY = new ApiError("LOGISTICS_PACKING_SKU_FNSKU_QTY_EXCEEDS_DELIVERY", 13520, "装箱中SKU【{0}】FnSku【{1}】累计装箱数量【{2}】不可大于发货数量【{3}】");
    public static final ApiError LOGISTICS_PACKING_TOTAL_QTY_EXCEEDS_DELIVERY = new ApiError("LOGISTICS_PACKING_TOTAL_QTY_EXCEEDS_DELIVERY", 13521, "装箱中SKU【{0}】累计装箱数量【{1}】不能大于发货数量【{2}】");
    public static final ApiError LOGISTICS_PACKING_PICKLIST_REQUIRED = new ApiError("LOGISTICS_PACKING_PICKLIST_REQUIRED", 13522, "调整装箱后，装箱数量不能为0，请检查装箱明细");
    public static final ApiError LOGISTICS_PACKING_SELECT_PICKLIST_REQUIRED = new ApiError("LOGISTICS_PACKING_SELECT_PICKLIST_REQUIRED", 13523, "请先选择拣货单");
    public static final ApiError LOGISTICS_ORDER_EXISTS_REVERSE_FORBIDDEN = new ApiError("LOGISTICS_ORDER_EXISTS_REVERSE_FORBIDDEN", 13524, "物流单【{0}】已生成，不允许执行反审核");
    public static final ApiError LOGISTICS_DECLARE_BILL_EXISTS_REVERSE_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_BILL_EXISTS_REVERSE_FORBIDDEN", 13525, "报关单【{0}】已生成，不允许执行反审核");
    public static final ApiError LOGISTICS_FNSKU_LABEL_PRINT_FAILED = new ApiError("LOGISTICS_FNSKU_LABEL_PRINT_FAILED", 13526, "打印FNSKU标签失败");
    public static final ApiError LOGISTICS_CUSTOMER_SKU_LABEL_PRINT_FAILED = new ApiError("LOGISTICS_CUSTOMER_SKU_LABEL_PRINT_FAILED", 13527, "打印客户SKU标签失败");
    public static final ApiError LOGISTICS_PACKING_NOT_COMPLETED_DECLARATION_FORBIDDEN = new ApiError("LOGISTICS_PACKING_NOT_COMPLETED_DECLARATION_FORBIDDEN", 13528, "装箱未完成，不允许下推报关单");
    public static final ApiError LOGISTICS_FIRST_MILE_ORDER_EXISTS_NOT_DEL = new ApiError("LOGISTICS_FIRST_MILE_ORDER_EXISTS_NOT_DEL", 13529, "物流单【{0}】已生成，不允许删除");
    public static final ApiError LOGISTICS_DECLARE_BILL_EXISTS_NOT_DEL = new ApiError("LOGISTICS_DECLARE_BILL_EXISTS_NOT_DEL", 13530, "报关单【{0}】已生成，不允许删除");
    public static final ApiError LOGISTICS_CHANNEL_BLACKLIST = new ApiError("LOGISTICS_CHANNEL_BLACKLIST", 13531, "物流渠道【{0}】不允许发往指定地区【{1}{2}{3}{4}{5}】，请调整物流渠道");
    public static final ApiError LOGISTICS_CHANNEL_COUNTRY_BLACKLIST = new ApiError("LOGISTICS_CHANNEL_COUNTRY_BLACKLIST", 13532, "物流渠道【{0}】不支持目的国家【{1}】");
    public static final ApiError LOGISTICS_CANCEL_NOT_SUPPORTED = new ApiError("LOGISTICS_CANCEL_NOT_SUPPORTED", 13533, "当前物流渠道不支持取消物流单【{0}】");
    public static final ApiError LOGISTICS_CANCEL_FAILED = new ApiError("LOGISTICS_CANCEL_FAILED", 13534, "原物流订单取消失败，请联系物流商处理后重试");
    public static final ApiError LOGISTICS_PLATFORM_WAREHOUSE_NOT_INTERCEPT = new ApiError("LOGISTICS_PLATFORM_WAREHOUSE_NOT_INTERCEPT", 13535, "平台仓订单不支持物流拦截操作");
    public static final ApiError LOGISTICS_CHANNEL_REQUIRED_FOR_CANCEL = new ApiError("LOGISTICS_CHANNEL_REQUIRED_FOR_CANCEL", 13536, "取消物流单时，物流渠道不能为空");
    public static final ApiError LOGISTICS_NOT_INTERCEPTED_CANNOT_CANCEL = new ApiError("LOGISTICS_NOT_INTERCEPTED_CANNOT_CANCEL", 13537, "未标记拦截的订单不支持取消拦截");
    public static final ApiError LOGISTICS_INTERCEPT_STATUS_INVALID = new ApiError("LOGISTICS_INTERCEPT_STATUS_INVALID", 13538, "物流商处理状态为空，仅未处理状态允许取消拦截");
    public static final ApiError LOGISTICS_INTERCEPT_PROCESSING_FORBIDDEN_CANCEL = new ApiError("LOGISTICS_INTERCEPT_PROCESSING_FORBIDDEN_CANCEL", 13539, "订单拦截处理中或已完成，无法取消拦截");
    public static final ApiError LOGISTICS_DECLARE_INFO_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_INFO_NOT_FOUND", 13540, "申报信息不存在");
    public static final ApiError LOGISTICS_DECLARE_SKU_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_SKU_NOT_FOUND", 13541, "销售订单【{0}】申报信息中未找到SKU");
    public static final ApiError LOGISTICS_DECLARE_CN_NAME_REQUIRED = new ApiError("LOGISTICS_DECLARE_CN_NAME_REQUIRED", 13542, "申报信息中SKU【{0}】的报关中文名不能为空");
    public static final ApiError LOGISTICS_DECLARE_EN_NAME_REQUIRED = new ApiError("LOGISTICS_DECLARE_EN_NAME_REQUIRED", 13543, "申报信息中SKU【{0}】的报关英文名不能为空");
    public static final ApiError LOGISTICS_DECLARE_PRICE_REQUIRED = new ApiError("LOGISTICS_DECLARE_PRICE_REQUIRED", 13544, "申报信息中SKU【{0}】目的国申报价不能为0");
    public static final ApiError LOGISTICS_DECLARE_CURRENCY_REQUIRED = new ApiError("LOGISTICS_DECLARE_CURRENCY_REQUIRED", 13545, "申报信息中SKU【{0}】目的国申报价币种不存在");
    public static final ApiError LOGISTICS_DECLARE_CURRENCY_SYMBOL_REQUIRED = new ApiError("LOGISTICS_DECLARE_CURRENCY_SYMBOL_REQUIRED", 13546, "申报信息中SKU【{0}】目的国申报价币种符号不存在");
    public static final ApiError LOGISTICS_DECLARE_WEIGHT_REQUIRED = new ApiError("LOGISTICS_DECLARE_WEIGHT_REQUIRED", 13547, "申报信息中SKU【{0}】重量不能为0");
    public static final ApiError LOGISTICS_DECLARE_CUSTOMS_INFO_REQUIRED = new ApiError("LOGISTICS_DECLARE_CUSTOMS_INFO_REQUIRED", 13548, "申报信息中SKU【{0}】目的国申报信息不存在");
    public static final ApiError LOGISTICS_SHIPPING_TEMPLATE_NOT_FOUND = new ApiError("LOGISTICS_SHIPPING_TEMPLATE_NOT_FOUND", 13549, "运费模板不存在");
    public static final ApiError LOGISTICS_SHIPPING_DEST_COUNTRY_REQUIRED = new ApiError("LOGISTICS_SHIPPING_DEST_COUNTRY_REQUIRED", 13550, "运费规则目的地不能为空");
    public static final ApiError LOGISTICS_SHIPPING_REGION_REQUIRED = new ApiError("LOGISTICS_SHIPPING_REGION_REQUIRED", 13551, "运费规则城市分区不能为空");
    public static final ApiError LOGISTICS_SHIPPING_CITY_REQUIRED = new ApiError("LOGISTICS_SHIPPING_CITY_REQUIRED", 13552, "运费规则城市不能为空");
    public static final ApiError LOGISTICS_SHIPPING_WAREHOUSE_REQUIRED = new ApiError("LOGISTICS_SHIPPING_WAREHOUSE_REQUIRED", 13553, "运费规则仓库不能为空");
    public static final ApiError LOGISTICS_FIRST_WEIGHT_REQUIRED = new ApiError("LOGISTICS_FIRST_WEIGHT_REQUIRED", 13554, "运费规则首重不能为空");
    public static final ApiError LOGISTICS_FIRST_WEIGHT_COST_REQUIRED = new ApiError("LOGISTICS_FIRST_WEIGHT_COST_REQUIRED", 13555, "运费规则首重运费不能为空");
    public static final ApiError LOGISTICS_ADDITIONAL_UNIT_WEIGHT_REQUIRED = new ApiError("LOGISTICS_ADDITIONAL_UNIT_WEIGHT_REQUIRED", 13556, "运费规则续重单位重量不能为空");
    public static final ApiError LOGISTICS_ADDITIONAL_UNIT_PRICE_REQUIRED = new ApiError("LOGISTICS_ADDITIONAL_UNIT_PRICE_REQUIRED", 13557, "运费规则续重单价不能为空");
    public static final ApiError LOGISTICS_SHIPPING_OTHER_COST_NOT_FOUND = new ApiError("LOGISTICS_SHIPPING_OTHER_COST_NOT_FOUND", 13558, "运费模板其他费用");
    public static final ApiError LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DISABLED_FORBIDDEN = new ApiError("LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DISABLED_FORBIDDEN", 13559, "运费模板被渠道引用不支持停用/启用");
    public static final ApiError LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DELETE_FORBIDDEN = new ApiError("LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DELETE_FORBIDDEN", 13560, "运费模板被渠道引用不支持删除");
    public static final ApiError LOGISTICS_SHIPPING_TEMPLATE_ALREADY_EXISTS = new ApiError("LOGISTICS_SHIPPING_TEMPLATE_ALREADY_EXISTS", 13561, "运费模板已存在");
    public static final ApiError LOGISTICS_TEMPLATE_RULE_WEIGHT_COUNTRY_OVERLAP = new ApiError("LOGISTICS_TEMPLATE_RULE_WEIGHT_COUNTRY_OVERLAP", 13562, "起始国【{0}】、目的国【{1}】的运费规则重量区间存在重叠");
    public static final ApiError LOGISTICS_TEMPLATE_RULE_WEIGHT_REGION_OVERLAP = new ApiError("LOGISTICS_TEMPLATE_RULE_WEIGHT_REGION_OVERLAP", 13563, "起始国【{0}】、目的国【{1}】、城市分区【{2}】的运费规则重量区间存在重叠");
    public static final ApiError LOGISTICS_TEMPLATE_RULE_WEIGHT_WAREHOUSE_OVERLAP = new ApiError("LOGISTICS_TEMPLATE_RULE_WEIGHT_WAREHOUSE_OVERLAP", 13564, "起始国【{0}】、目的仓库【{1}】的运费规则重量区间存在重叠");
    public static final ApiError LOGISTICS_SHIPPING_RULE_NOT_FOUND = new ApiError("LOGISTICS_SHIPPING_RULE_NOT_FOUND", 13565, "未找到运费规则");
    public static final ApiError LOGISTICS_WEIGHT_OUT_OF_RANGE = new ApiError("LOGISTICS_WEIGHT_OUT_OF_RANGE", 13566, "重量【{0}】不在开始重量【{1}】与结束重量【{2}】之间");
    public static final ApiError LOGISTICS_OTHER_COST_SETTING_NOT_FOUND = new ApiError("LOGISTICS_OTHER_COST_SETTING_NOT_FOUND", 13567, "未找到其他费用【{0}】的计算方式");
    public static final ApiError LOGISTICS_ADDRESS_NAME_ALREADY_EXISTS = new ApiError("LOGISTICS_ADDRESS_NAME_ALREADY_EXISTS", 13568, "物流地址名称【{0}】已存在");
    public static final ApiError LOGISTICS_UNIT_PRICE_REQUIRED = new ApiError("LOGISTICS_UNIT_PRICE_REQUIRED", 13569, "运费规则运费单价不能为空");
    public static final ApiError LOGISTICS_CANCEL_AUTH_NOT_ALLOWED = new ApiError("LOGISTICS_CANCEL_AUTH_NOT_ALLOWED", 13570, "仅已授权状态才允许取消授权");
    public static final ApiError LOGISTICS_SYNC_FORBIDDEN_NOT_AUTHORIZED = new ApiError("LOGISTICS_SYNC_FORBIDDEN_NOT_AUTHORIZED", 13571, "物流商未授权，不允许同步渠道");
    public static final ApiError LOGISTICS_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN = new ApiError("LOGISTICS_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN", 13572, "存在未停用的物流渠道，无法停用该物流商");
    public static final ApiError LOGISTICS_CHANNEL_ADDRESS_REF_DELETE_FORBIDDEN = new ApiError("LOGISTICS_CHANNEL_ADDRESS_REF_DELETE_FORBIDDEN", 13573, "地址【{0}】已被渠道引用，不支持删除");
    public static final ApiError LOGISTICS_CHANNEL_NOT_FOUND = new ApiError("LOGISTICS_CHANNEL_NOT_FOUND", 13574, "物流渠道不存在");
    public static final ApiError LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY = new ApiError("LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY", 13575, "渠道【{0}】下类型【{1}】的地址为空");
    public static final ApiError LOGISTICS_SALES_CHANNEL_NOT_CONFIGURED = new ApiError("LOGISTICS_SALES_CHANNEL_NOT_CONFIGURED", 13576, "渠道【{0}】尚未配置销售渠道");
    public static final ApiError LOGISTICS_PRINT_WAYBILL_FAILED = new ApiError("LOGISTICS_PRINT_WAYBILL_FAILED", 13577, "调用第三方接口打印面单异常，订单ID:{0}，原因：{1}");
    public static final ApiError LOGISTICS_CHANNEL_QUOTE_REF_DELETE_FORBIDDEN = new ApiError("LOGISTICS_CHANNEL_QUOTE_REF_DELETE_FORBIDDEN", 13578, "该物流渠道已被引用，不支持删除");
    public static final ApiError LOGISTICS_SELF_SHIP_BILL_STATUS_CHANGE_FORBIDDEN = new ApiError("LOGISTICS_SELF_SHIP_BILL_STATUS_CHANGE_FORBIDDEN", 13579, "已确认或已作废的自发货费用单不支持状态变更");
    public static final ApiError LOGISTICS_CHANNEL_ALREADY_USED = new ApiError("LOGISTICS_CHANNEL_ALREADY_USED", 13580, "物流渠道【{0}】已被使用，不支持重复选择");
    public static final ApiError LOGISTICS_SAILING_CONFIG_ALREADY_EXISTS = new ApiError("LOGISTICS_SAILING_CONFIG_ALREADY_EXISTS", 13581, "已存在渠道【{0}】的截单开船配置数据");
    public static final ApiError LOGISTICS_COST_NAME_ALREADY_EXISTS = new ApiError("LOGISTICS_COST_NAME_ALREADY_EXISTS", 13582, "费用归属【{0}】费用名称【{1}】已存在");
    public static final ApiError LOGISTICS_WAREHOUSE_MAPPING_ALREADY_EXISTS = new ApiError("LOGISTICS_WAREHOUSE_MAPPING_ALREADY_EXISTS", 13583, "物流商仓库代码【{0}】已存在");
    public static final ApiError LOGISTICS_DECLARE_RECONCILIATION_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_RECONCILIATION_NOT_FOUND", 13584, "报关对账单不存在");
    public static final ApiError LOGISTICS_DECLARE_RECONCILIATION_DETAIL_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_RECONCILIATION_DETAIL_NOT_FOUND", 13585, "报关对账单明细不存在");
    public static final ApiError LOGISTICS_DECLARE_RECONCILIATION_SUPPLIER_MISMATCH = new ApiError("LOGISTICS_DECLARE_RECONCILIATION_SUPPLIER_MISMATCH", 13586, "报关对账单【{0}】新增对账明细的供应商【{1}】必须保持一致");
    public static final ApiError LOGISTICS_TRANSFER_SUPPLIER_NOT_FOUND_NOT_PACKAGE = new ApiError("LOGISTICS_TRANSFER_SUPPLIER_NOT_FOUND_NOT_PACKAGE", 13587, "订单中转物流商不存在，请重新预报后再扫描");
    public static final ApiError LOGISTICS_ORDER_VOIDED_NOT_PACKAGE = new ApiError("LOGISTICS_ORDER_VOIDED_NOT_PACKAGE", 13588, "订单已作废，不允许组包");
    public static final ApiError LOGISTICS_TRANSFER_ORDER_INVALID_NOT_PACKAGE = new ApiError("LOGISTICS_TRANSFER_ORDER_INVALID_NOT_PACKAGE", 13589, "中转报关订单处于待中转或上传失败状态，不允许组包发货");
    public static final ApiError LOGISTICS_ORDER_INTERCEPTED_NOT_PACKAGE = new ApiError("LOGISTICS_ORDER_INTERCEPTED_NOT_PACKAGE", 13590, "订单已被拦截，不允许操作");
    public static final ApiError LOGISTICS_CHANNEL_AUTH_INFO_NOT_FOUND = new ApiError("LOGISTICS_CHANNEL_AUTH_INFO_NOT_FOUND", 13591, "物流渠道未匹配到授权信息");
    public static final ApiError LOGISTICS_DELIVERY_SUPPLIER_DUPLICATE = new ApiError("LOGISTICS_DELIVERY_SUPPLIER_DUPLICATE", 13592, "发货物流商不可重复设置，每个发货物流商仅允许配置一个报关规则");
    public static final ApiError LOGISTICS_TRANSFER_SUPPLIER_DUPLICATE = new ApiError("LOGISTICS_TRANSFER_SUPPLIER_DUPLICATE", 13593, "中转物流商不可重复设置，每个中转物流商仅允许配置一个截单规则");
    public static final ApiError LOGISTICS_GENERATE_TIME_AFTER_DEADLINE_FORBIDDEN = new ApiError("LOGISTICS_GENERATE_TIME_AFTER_DEADLINE_FORBIDDEN", 13594, "生成时间不能晚于截单时间");
    public static final ApiError LOGISTICS_TRACK_STATUS_SYSTEM_MANAGED_NOT_EDITABLE = new ApiError("LOGISTICS_TRACK_STATUS_SYSTEM_MANAGED_NOT_EDITABLE", 13595, "该运输状态由系统维护，不允许手动修改");
    public static final ApiError LOGISTICS_TRANSFER_SUPPLIER_REF_DELETE_FORBIDDEN = new ApiError("LOGISTICS_TRANSFER_SUPPLIER_REF_DELETE_FORBIDDEN", 13596, "被其他单据引用的中转物流商不允许删除");
    public static final ApiError LOGISTICS_PRODUCT_NOT_REGISTERED = new ApiError("LOGISTICS_PRODUCT_NOT_REGISTERED", 13597, "产品【{0}】未在平台【{1}】完成备案，请联系关务或物流");
    public static final ApiError LOGISTICS_CHANNEL_CHANGE_FORBIDDEN_NOT_REGISTERED = new ApiError("LOGISTICS_CHANNEL_CHANGE_FORBIDDEN_NOT_REGISTERED", 13598, "产品【{0}】未在平台【{1}】备案，无法切换渠道【{2}】，请联系关务或物流");
    public static final ApiError LOGISTICS_TRANSFER_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN = new ApiError("LOGISTICS_TRANSFER_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN", 13599, "存在未停用的中转物流渠道，无法停用该物流商");
    public static final ApiError LOGISTICS_PACKAGE_DIMENSION_REQUIRED = new ApiError("LOGISTICS_PACKAGE_DIMENSION_REQUIRED", 13600, "长、宽、高的单个值不能为空且必须大于0");
    public static final ApiError LOGISTICS_PRINT_WAYBILL_NOT_SUPPORTED = new ApiError("LOGISTICS_PRINT_WAYBILL_NOT_SUPPORTED", 13601, "物流商【{0}】不支持打印物流面单");
    public static final ApiError LOGISTICS_PRINT_ALLOCATE_CARGO_NOT_SUPPORTED = new ApiError("LOGISTICS_PRINT_ALLOCATE_CARGO_NOT_SUPPORTED", 13602, "物流商【{0}】不支持单独打印官方配货单");
    public static final ApiError LOGISTICS_PRINT_SETTING_NOT_FOUND = new ApiError("LOGISTICS_PRINT_SETTING_NOT_FOUND", 13603, "渠道【{0}】配置的配货单打印类型未找到");
    public static final ApiError LOGISTICS_COST_CONFIG_NOT_FOUND = new ApiError("LOGISTICS_COST_CONFIG_NOT_FOUND", 13604, "未发现费用【{0}】的配置信息");
    public static final ApiError LOGISTICS_CHANNEL_WAREHOUSE_REQUIRED = new ApiError("LOGISTICS_CHANNEL_WAREHOUSE_REQUIRED", 13605, "指定仓库不能为空");
    public static final ApiError LOGISTICS_CONFIG_NOT_FOUND = new ApiError("LOGISTICS_CONFIG_NOT_FOUND", 13606, "物流配置不存在");
    public static final ApiError LOGISTICS_CALL_THIRD_PLATFORM_ERROR = new ApiError("LOGISTICS_CALL_THIRD_PLATFORM_ERROR", 13607, "调用第三方物流平台接口异常");
    public static final ApiError LOGISTICS_LABEL_TYPE_REQUIRED = new ApiError("LOGISTICS_LABEL_TYPE_REQUIRED", 13608, "标签类型不能为空");
    public static final ApiError LOGISTICS_LARGE_TABLE_EXISTS = new ApiError("LOGISTICS_LARGE_TABLE_EXISTS", 13609, "已生成物流大表不能重复生成");
    public static final ApiError LOGISTICS_LARGE_ESTIMATED_EXISTS = new ApiError("LOGISTICS_LARGE_ESTIMATED_EXISTS", 13610, "已存在预估账单的物流大表信息，请不要重复下推");
    public static final ApiError LOGISTICS_SMALL_BAG_NOT_CONFIRMED = new ApiError("LOGISTICS_SMALL_BAG_NOT_CONFIRMED", 13611, "小包费用分摊未确认，不能生成物流大表");
    public static final ApiError LOGISTICS_SELF_SHIP_FEE_NOT_FOUND = new ApiError("LOGISTICS_SELF_SHIP_FEE_NOT_FOUND", 13612, "自发货费用不存在");
    public static final ApiError LOGISTICS_ACTUAL_EXISTS_CANNOT_PUSH = new ApiError("LOGISTICS_ACTUAL_EXISTS_CANNOT_PUSH", 13613, "已存在实际账单，不能再下推实际账单");
    public static final ApiError LOGISTICS_MAPPING_NOT_NULL = new ApiError("LOGISTICS_MAPPING_NOT_NULL", 13614, "【{0}】所属的平台【{1}】没有配置【{2}】的标发信息，不允许提交发货");
    public static final ApiError LOGISTICS_SUPPLIER_NOT_FOUND = new ApiError("LOGISTICS_SUPPLIER_NOT_FOUND", 13615, "头程费用分摊物流商为空");
    public static final ApiError LOGISTICS_SUPPLIER_NOT_EXIST = new ApiError("LOGISTICS_SUPPLIER_NOT_EXIST", 13616, "头程费用分摊物流商不存在");
    public static final ApiError LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND = new ApiError("LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND", 13617, "导入文件名称不能为空");
    public static final ApiError LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND = new ApiError("LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND", 13618, "导入的物流配置明细不能为空");
    public static final ApiError LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND = new ApiError("LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND", 13619, "文件【{0}】导入的物流配置明细唯一键未找到");
    public static final ApiError LOGISTICS_SUPPLIER_NAME_NOT_FOUND = new ApiError("LOGISTICS_SUPPLIER_NAME_NOT_FOUND", 13620, "物流商名称【{0}】未找到");
    public static final ApiError LOGISTICS_BILL_COST_IMPORT_RECORD_UNIQUE_KEY_ERROR = new ApiError("LOGISTICS_BILL_COST_IMPORT_RECORD_UNIQUE_KEY_ERROR", 13621, "导入唯一识别单号查询失败，请检查识别单号配置");
    public static final ApiError LOGISTICS_ASYNC_TASK_CREATE_ERROR = new ApiError("LOGISTICS_ASYNC_TASK_CREATE_ERROR", 13622, "异步任务已存在参数【{0}】");
    public static final ApiError LOGISTICS_PENDING_COST_NOT_FOUND = new ApiError("LOGISTICS_PENDING_COST_NOT_FOUND", 13623, "待确认费用分摊记录不存在");
    public static final ApiError LOGISTICS_SELECT_AT_LEAST_ONE = new ApiError("LOGISTICS_SELECT_AT_LEAST_ONE", 13624, "明细至少勾选一个识别单号");
    public static final ApiError LOGISTICS_BILL_FIELD_DUPLICATE_NOT_ALLOWED = new ApiError("LOGISTICS_BILL_FIELD_DUPLICATE_NOT_ALLOWED", 13625, "数大臣单据字段【{0}】不允许重复");
    public static final ApiError LOGISTICS_BILL_DETAIL_FIELD_REQUIRED = new ApiError("LOGISTICS_BILL_DETAIL_FIELD_REQUIRED", 13626, "数大臣单据明细字段不允许为空");
    public static final ApiError LOGISTICS_SMALL_BAG_NOT_CAN_Allocate = new ApiError("LOGISTICS_SMALL_BAG_NOT_CAN_Allocate", 13627, "费用分摊设置为不分摊，不能生成小包费用分摊");
    public static final ApiError LOGISTICS_BILL_COST_IMPORT_RECORD_HEAD_NOTFOUND = new ApiError("LOGISTICS_BILL_COST_IMPORT_RECORD_HEAD_NOTFOUND", 13628, "导入未匹配到表头字段，请检查费用配置");
    public static final ApiError LOGISTICS_BILL_UNIQUE_FIELD_NOT_ALLOWED = new ApiError("LOGISTICS_BILL_UNIQUE_FIELD_NOT_ALLOWED", 13629, "【{0}】不能作为识别单号字段");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_PUSH_TYPE_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_PUSH_TYPE_REQUIRED", 13630, "推送类型不能为空");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_SAVE_FAILED = new ApiError("LOGISTICS_THIRD_CHANNEL_SAVE_FAILED", 13631, "物流-第三方渠道关系单保存失败");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_NOT_FOUND = new ApiError("LOGISTICS_THIRD_CHANNEL_NOT_FOUND", 13632, "未找到渠道配置数据");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_IN_USE_DELETE_FORBIDDEN = new ApiError("LOGISTICS_THIRD_CHANNEL_IN_USE_DELETE_FORBIDDEN", 13633, "该渠道配置已被使用，不能删除");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_STATUS_UNCHANGED = new ApiError("LOGISTICS_THIRD_CHANNEL_STATUS_UNCHANGED", 13634, "渠道配置数据状态未变更");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_DUPLICATE = new ApiError("LOGISTICS_THIRD_CHANNEL_DUPLICATE", 13635, "同一个平台下我司物流商【{0}】+渠道【{1}】，查询物流商+渠道仅可创建一条");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_SUPPLIER_NOT_FOUND = new ApiError("LOGISTICS_THIRD_CHANNEL_SUPPLIER_NOT_FOUND", 13636, "物流商不存在");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_CHANNEL_NOT_FOUND = new ApiError("LOGISTICS_THIRD_CHANNEL_CHANNEL_NOT_FOUND", 13637, "物流商渠道不存在");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_QUERY_PROVIDER_NOT_FOUND = new ApiError("LOGISTICS_THIRD_CHANNEL_QUERY_PROVIDER_NOT_FOUND", 13638, "查询物流商【{0}】不存在");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_PUSH_MOBILE_IMMUTABLE = new ApiError("LOGISTICS_THIRD_CHANNEL_PUSH_MOBILE_IMMUTABLE", 13639, "是否推送电话不能修改");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_MOBILE_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_MOBILE_REQUIRED", 13640, "手机号码不能为空");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_SHOP_ID_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_SHOP_ID_REQUIRED", 13641, "店铺Id不能为空");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_PLATFORM_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_PLATFORM_REQUIRED", 13642, "平台不能为空");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_DETAIL_NOT_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_DETAIL_NOT_REQUIRED", 13643, "推送明细不需要配置");
    public static final ApiError LOGISTICS_THIRD_CHANNEL_QUERY_SUPPLIER_NAME_REQUIRED = new ApiError("LOGISTICS_THIRD_CHANNEL_QUERY_SUPPLIER_NAME_REQUIRED", 13644, "查询物流商(中文)不能为空");
    public static final ApiError LOGISTICS_ORDER_NOT_CANCEL = new ApiError("LOGISTICS_ORDER_NOT_CANCEL", 13645, "物流单据不是已取消或者下单失败状态，不能编辑");
    public static final ApiError LOGISTICS_ORDER_CANNOT_EDIT = new ApiError("LOGISTICS_ORDER_CANNOT_EDIT", 13646, "该单据不能再当前页面编辑");
    public static final ApiError LOGISTICS_CHANNEL_CODE_EMPTY = new ApiError("LOGISTICS_CHANNEL_CODE_EMPTY", 13647, "渠道代码为空或者格式不正确");
    public static final ApiError LOGISTICS_DECLARE_BILL_EXISTS_NOT_SOURCE = new ApiError("LOGISTICS_DECLARE_BILL_EXISTS_NOT_SOURCE", 13630, "报关单未找到来源信息");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_DETAIL_MATCH_FAILED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_DETAIL_MATCH_FAILED", 13630, "{0}明细匹配失败，商品编码：{1}");
    public static final ApiError LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND = new ApiError("LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND", 13631, "未找到商品物流信息，商品编码：{0}");
    public static final ApiError LOGISTICS_PRODUCT_LOGISTIC_DECLARE_INFO_INCOMPLETE = new ApiError("LOGISTICS_PRODUCT_LOGISTIC_DECLARE_INFO_INCOMPLETE", 13632, "商品物流申报信息不完整，商品编码：{0}");
    public static final ApiError LOGISTICS_PRODUCT_LOGISTIC_DECLARE_PRICE_REQUIRED = new ApiError("LOGISTICS_PRODUCT_LOGISTIC_DECLARE_PRICE_REQUIRED", 13633, "商品物流申报单价缺失，商品编码：{0}");
    public static final ApiError LOGISTICS_COMBO_DECLARE_CHILD_EMPTY = new ApiError("LOGISTICS_COMBO_DECLARE_CHILD_EMPTY", 13634, "组合品拆分子件为空，商品编码：{0}");
    public static final ApiError LOGISTICS_COMBO_DECLARE_CHILD_QTY_EMPTY = new ApiError("LOGISTICS_COMBO_DECLARE_CHILD_QTY_EMPTY", 13635, "组合品拆分子件数量为空，商品编码：{0}");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_AUTO_GENERATE_FAILED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_AUTO_GENERATE_FAILED", 13636, "{0}【{1}】自动生成报关明细中间表失败：{2}");
    public static final ApiError LOGISTICS_DECLARE_BILL_AUTO_GENERATE_FAILED = new ApiError("LOGISTICS_DECLARE_BILL_AUTO_GENERATE_FAILED", 13637, "{0}【{1}】自动生成报关单失败：{2}");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED", 13638, "请选择需要预览的报关明细");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND", 13639, "部分报关明细中间表数据不存在");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT", 13640, "仅支持未生成和待确认的报关明细预览");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT", 13641, "不同来源类型的报关明细不能合并预览");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT", 13642, "不同业务单号目的国不一致，不能合并预览");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND", 13643, "未找到匹配的报关规则");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SENDER_TYPE_CONFLICT = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SENDER_TYPE_CONFLICT", 13644, "报关规则发货人类型不一致，不能合并预览");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED", 13645, "至少选择两条明细进行合并");
    public static final ApiError LOGISTICS_DECLARE_STATUS_UPDATE_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_STATUS_UPDATE_FORBIDDEN", 13646, "报关状态不允许从【{0}】更新为【{1}】");
    public static final ApiError LOGISTICS_DECLARE_STATUS_CONFIRM_DATE_REQUIRED = new ApiError("LOGISTICS_DECLARE_STATUS_CONFIRM_DATE_REQUIRED", 13647, "报关确认日期不能为空");
    public static final ApiError LOGISTICS_DECLARE_STATUS_CONFIRM_USER_REQUIRED = new ApiError("LOGISTICS_DECLARE_STATUS_CONFIRM_USER_REQUIRED", 13648, "报关确认人不能为空");
    public static final ApiError LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED = new ApiError("LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED", 13649, "报关明细【{0}】不能为空");
    public static final ApiError LOGISTICS_DECLARE_STATUS_DETAIL_INCONSISTENT = new ApiError("LOGISTICS_DECLARE_STATUS_DETAIL_INCONSISTENT", 13650, "报关明细【{0}】必须保持一致");
    public static final ApiError LOGISTICS_DECLARE_BILL_TYPE_MISMATCH = new ApiError("LOGISTICS_DECLARE_BILL_TYPE_MISMATCH", 13651, "报关单类型不匹配");
    public static final ApiError LOGISTICS_DECLARE_STATUS_INVALID_TARGET = new ApiError("LOGISTICS_DECLARE_STATUS_INVALID_TARGET", 13652, "报关状态不存在");
    public static final ApiError LOGISTICS_DECLARE_MERGE_RECEIVER_TYPE_DIFF = new ApiError("LOGISTICS_DECLARE_MERGE_RECEIVER_TYPE_DIFF", 13653, "报关单合并收货人类型必须一致");
    public static final ApiError LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_NOT_FOUND", 13654, "请先配置报关单头编码规则");
    public static final ApiError LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_FAILED = new ApiError("LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_FAILED", 13655, "核算公司【{0}】未配置编码规则");
    public static final ApiError LOGISTICS_DECLARE_SENDER_REQUIRED = new ApiError("LOGISTICS_DECLARE_SENDER_REQUIRED", 13718, "报关单发货人不能为空");
    public static final ApiError LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE = new ApiError("LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE", 13656, "报关规则发货人和收货人组合不能重复");
    public static final ApiError LOGISTICS_DECLARE_RULE_CONDITION_DUPLICATE = new ApiError("LOGISTICS_DECLARE_RULE_CONDITION_DUPLICATE", 13657, "报关规则条件字段【{0}】的值【{1}】不能重复");
    public static final ApiError LOGISTICS_DECLARE_RULE_SAVE_FAILED = new ApiError("LOGISTICS_DECLARE_RULE_SAVE_FAILED", 13658, "报关规则配置保存失败");
    public static final ApiError LOGISTICS_DECLARE_GENERATABLE_DELIVERY_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_GENERATABLE_DELIVERY_NOT_FOUND", 13659, "没有可生成报关单的发货单");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED", 13660, "请选择需要保存的报关明细");
    public static final ApiError LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND", 13661, "没有可生成报关单的明细");
    public static final ApiError LOGISTICS_DECLARE_BILL_SAVE_FAILED = new ApiError("LOGISTICS_DECLARE_BILL_SAVE_FAILED", 13662, "报关单保存失败");
    public static final ApiError LOGISTICS_DECLARE_WAIT_STATUS_REQUIRED_FOR_EDIT = new ApiError("LOGISTICS_DECLARE_WAIT_STATUS_REQUIRED_FOR_EDIT", 13663, "报关单状态不是待确认，不能编辑");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_SAVE_FAILED = new ApiError("LOGISTICS_DECLARE_DETAIL_SAVE_FAILED", 13664, "报关单明细保存失败");
    public static final ApiError LOGISTICS_DECLARE_DELIVERY_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_DELIVERY_NOT_FOUND", 13665, "未找到发货单信息");
    public static final ApiError LOGISTICS_DECLARE_SO_OUT_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_SO_OUT_NOT_FOUND", 13666, "未找到销售出库单信息");
    public static final ApiError LOGISTICS_DECLARE_STATUS_INVALID = new ApiError("LOGISTICS_DECLARE_STATUS_INVALID", 13667, "报关状态无效");
    public static final ApiError LOGISTICS_DECLARE_SELECTED_SKU_REQUIRED = new ApiError("LOGISTICS_DECLARE_SELECTED_SKU_REQUIRED", 13668, "选中的SKU信息不能为空");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_HEADER_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_SOURCE_HEADER_NOT_FOUND", 13669, "未找到来源单据，无法查询报关表头");
    public static final ApiError LOGISTICS_DECLARE_GENERATABLE_SO_DELIVERY_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_GENERATABLE_SO_DELIVERY_NOT_FOUND", 13670, "没有可生成报关单的销售发货通知单");
    public static final ApiError LOGISTICS_DECLARE_BATCH_UPDATE_FAILED = new ApiError("LOGISTICS_DECLARE_BATCH_UPDATE_FAILED", 13671, "报关单批量更新失败");
    public static final ApiError LOGISTICS_DECLARE_FIELD_DATE_FORMAT_INVALID = new ApiError("LOGISTICS_DECLARE_FIELD_DATE_FORMAT_INVALID", 13672, "{0}格式错误，请使用yyyy-MM-dd");
    public static final ApiError LOGISTICS_DECLARE_FIELD_NUMBER_FORMAT_INVALID = new ApiError("LOGISTICS_DECLARE_FIELD_NUMBER_FORMAT_INVALID", 13673, "{0}格式错误，请输入数字");
    public static final ApiError LOGISTICS_DECLARE_FM_SPLIT_VIEW_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_FM_SPLIT_VIEW_FORBIDDEN", 13674, "非头程报关单，无法查看拆分明细");
    public static final ApiError LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED = new ApiError("LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED", 13675, "仅待确认报关单可以拆分明细");
    public static final ApiError LOGISTICS_DECLARE_SPLIT_SINGLE_BILL_REQUIRED = new ApiError("LOGISTICS_DECLARE_SPLIT_SINGLE_BILL_REQUIRED", 13676, "批量添加拆分明细只能关联同一张报关单");
    public static final ApiError LOGISTICS_DECLARE_FM_SPLIT_ADD_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_FM_SPLIT_ADD_FORBIDDEN", 13677, "非头程报关单，无法添加拆分明细");
    public static final ApiError LOGISTICS_DECLARE_B2B_SPLIT_ADD_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_B2B_SPLIT_ADD_FORBIDDEN", 13678, "非B2B报关单，无法添加拆分明细");
    public static final ApiError LOGISTICS_DECLARE_MERGE_WAIT_STATUS_REQUIRED = new ApiError("LOGISTICS_DECLARE_MERGE_WAIT_STATUS_REQUIRED", 13679, "报关单【{0}】仅待确认状态可以操作合并");
    public static final ApiError LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE = new ApiError("LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE", 13680, "来源单【{0}】未找到匹配的报关规则，请检查来源单信息是否正确");
    public static final ApiError LOGISTICS_DECLARE_MERGE_SKU_LIMIT_EXCEEDED = new ApiError("LOGISTICS_DECLARE_MERGE_SKU_LIMIT_EXCEEDED", 13681, "合并后SKU条数不可超过{0}条");
    public static final ApiError LOGISTICS_DECLARE_MERGE_COUNTRY_MISMATCH = new ApiError("LOGISTICS_DECLARE_MERGE_COUNTRY_MISMATCH", 13682, "不同业务单号必须归属同一个国家");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_SOURCE_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_SOURCE_REQUIRED", 13683, "第{0}行来源明细不能为空");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_SOURCE_DUPLICATE = new ApiError("LOGISTICS_DECLARE_DETAIL_SOURCE_DUPLICATE", 13684, "第{0}行来源明细重复，请勿重复保存");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_QTY_MISMATCH = new ApiError("LOGISTICS_DECLARE_DETAIL_QTY_MISMATCH", 13685, "第{0}行数量必须等于来源明细数量合计");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE = new ApiError("LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE", 13686, "未找到来源明细，无法保存报关单");
    public static final ApiError LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED = new ApiError("LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED", 13687, "所选明细已生成报关单，请勿重复保存");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_GENERATED = new ApiError("LOGISTICS_DECLARE_SOURCE_GENERATED", 13688, "来源单【{0}】已生成报关单，请勿重复保存");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_BOX_IMMUTABLE = new ApiError("LOGISTICS_DECLARE_SOURCE_BOX_IMMUTABLE", 13689, "业务单号、SKU、箱号不可编辑");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_QTY_IMMUTABLE = new ApiError("LOGISTICS_DECLARE_SOURCE_QTY_IMMUTABLE", 13690, "业务单号、SKU、数量不可编辑");
    public static final ApiError LOGISTICS_DECLARE_DUPLICATE_SKU_ROW = new ApiError("LOGISTICS_DECLARE_DUPLICATE_SKU_ROW", 13691, "相同SKU【{0}】存在不同行明细：第{1}行、第{2}行");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_SOURCE_VALUE_MISMATCH = new ApiError("LOGISTICS_DECLARE_DETAIL_SOURCE_VALUE_MISMATCH", 13692, "第{0}行{1}与来源明细不一致");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED", 13693, "第{0}行{1}不能为空");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_FIELD_POSITIVE_REQUIRED = new ApiError("LOGISTICS_DECLARE_DETAIL_FIELD_POSITIVE_REQUIRED", 13694, "第{0}行{1}必须大于0");
    public static final ApiError LOGISTICS_DECLARE_REPLACE_TYPE_MISMATCH = new ApiError("LOGISTICS_DECLARE_REPLACE_TYPE_MISMATCH", 13695, "报关单【{0}】类型与当前保存不一致，无法合并替换");
    public static final ApiError LOGISTICS_DECLARE_REPLACE_WAIT_STATUS_REQUIRED = new ApiError("LOGISTICS_DECLARE_REPLACE_WAIT_STATUS_REQUIRED", 13696, "报关单【{0}】非待确认状态，无法合并替换");
    public static final ApiError LOGISTICS_DECLARE_BOX_SINGLE_BILL_REQUIRED = new ApiError("LOGISTICS_DECLARE_BOX_SINGLE_BILL_REQUIRED", 13697, "同一业务单同一箱号必须在同一个报关单：第{0}票、第{1}票");
    public static final ApiError LOGISTICS_DECLARE_B2B_SPLIT_VIEW_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_B2B_SPLIT_VIEW_FORBIDDEN", 13698, "非B2B报关单，无法查看拆分明细");
    public static final ApiError LOGISTICS_DECLARE_COMBO_CHILD_NOT_FULL_SELECTED = new ApiError("LOGISTICS_DECLARE_COMBO_CHILD_NOT_FULL_SELECTED", 13699, "来源单【{0}】箱号【{1}】组合品【{2}】未勾选完整，缺失子SKU：{3}");
    public static final ApiError LOGISTICS_DECLARE_BOM_HISTORY_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_BOM_HISTORY_NOT_FOUND", 13700, "来源单【{0}】箱号【{1}】子SKU【{2}】未找到BOM历史，bomHistoryId：{3}");
    public static final ApiError LOGISTICS_DECLARE_LATEST_PRODUCT_LOGISTIC_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_LATEST_PRODUCT_LOGISTIC_NOT_FOUND", 13701, "未找到最新PLM产品物流资料，商品编码：{0}");
    public static final ApiError LOGISTICS_PACKING_DELIVERY_CHECK_DECLARE_STATUS = new ApiError("LOGISTICS_PACKING_DELIVERY_CHECK_DECLARE_STATUS", 13701, "关联单号【{0}】已生成报关单，不能修改装箱信息");
    public static final ApiError LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED = new ApiError("LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED", 13702, "单据【{0}】SKU【{1}】缺少报关信息：{2}，请先到PLM物流产品资料完善后再生成报关单");
    public static final ApiError LOGISTICS_DECLARE_DEST_COUNTRY_CN_NOT_GENERATE = new ApiError("LOGISTICS_DECLARE_DEST_COUNTRY_CN_NOT_GENERATE", 13703, "单据【{0}】目的国为中国大陆，不生成报关单");
    public static final ApiError LOGISTICS_DECLARE_BOX_NOT_FULL_SELECTED = new ApiError("LOGISTICS_DECLARE_BOX_NOT_FULL_SELECTED", 13704, "业务单号【{0}】箱号【{1}】未勾选完整，缺失SKU：{2}");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RECEIVER_TYPE_CONFLICT = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RECEIVER_TYPE_CONFLICT", 13705, "境外收货人类型不一致，不能合并预览");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND_FOR_SOURCE = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND_FOR_SOURCE", 13706, "来源单【{0}】未找到匹配的报关规则");
    public static final ApiError LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED_BATCH = new ApiError("LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED_BATCH", 13707, "以下SKU缺少报关信息，请先到PLM物流产品资料完善后再生成报关单：{0}");
    public static final ApiError LOGISTICS_DECLARE_SPLIT_DETAIL_MISSING = new ApiError("LOGISTICS_DECLARE_SPLIT_DETAIL_MISSING", 13708, "拆分未覆盖全部明细，业务单号【{0}】箱号【{1}】未分配到任何一票，缺失SKU：{2}");
    public static final ApiError LOGISTICS_DECLARE_SOURCE_STATUS_SYNC_FAILED = new ApiError("LOGISTICS_DECLARE_SOURCE_STATUS_SYNC_FAILED", 13709, "回写{0}报关状态失败，sourceIds={1}");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_SAVE_FAILED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_SAVE_FAILED", 13710, "报关明细中间表保存失败");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_NOT_EXIST = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_NOT_EXIST", 13711, "报关明细中间表不存在");
    public static final ApiError LOGISTICS_DECLARE_DETAIL_MID_UPDATE_FAILED = new ApiError("LOGISTICS_DECLARE_DETAIL_MID_UPDATE_FAILED", 13712, "报关明细中间表更新失败");
    public static final ApiError LOGISTICS_DECLARE_B2B_SO_DETAIL_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_B2B_SO_DETAIL_NOT_FOUND", 13712, "来源单【{0}】SKU【{1}】未找到销售订单明细，无法按客户分发规则取价");
    public static final ApiError LOGISTICS_DECLARE_B2B_CUSTOMER_RECEIVER_NOT_FOUND = new ApiError("LOGISTICS_DECLARE_B2B_CUSTOMER_RECEIVER_NOT_FOUND", 13713, "来源单【{0}】未找到客户信息，无法按客户分发规则设置报关收货人");
    public static final ApiError LOGISTICS_DECLARE_RULE_TYPE_INVALID = new ApiError("LOGISTICS_DECLARE_RULE_TYPE_INVALID", 13714, "报关规则类型必须为fmDeclareBill或b2bDeclareBill");
    public static final ApiError LOGISTICS_DECLARE_RULE_TYPE_MISMATCH = new ApiError("LOGISTICS_DECLARE_RULE_TYPE_MISMATCH", 13715, "请求ruleType与明细ruleType不一致");
    public static final ApiError LOGISTICS_DECLARE_RULE_CROSS_TYPE_UPDATE_FORBIDDEN = new ApiError("LOGISTICS_DECLARE_RULE_CROSS_TYPE_UPDATE_FORBIDDEN", 13716, "不允许跨ruleType更新");
    public static final ApiError LOGISTICS_DECLARE_RULE_ACCOUNTING_COMPANY_LOAD_FAILED = new ApiError("LOGISTICS_DECLARE_RULE_ACCOUNTING_COMPANY_LOAD_FAILED", 13717, "加载核算公司列表失败");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_CONFIG_REQUIRED = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_CONFIG_REQUIRED", 13720, "合同协议号配置不能为空");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_REQUIRED = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_REQUIRED", 13721, "核算公司不能为空");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_REQUIRED = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_REQUIRED", 13722, "合同协议号不能为空");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_LETTERS_ONLY = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_LETTERS_ONLY", 13723, "合同协议号只能输入英文字母");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_INVALID = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_INVALID", 13724, "核算公司不存在或已禁用");
    public static final ApiError LOGISTICS_CONTRACT_AGREEMENT_NO_DUPLICATE = new ApiError("LOGISTICS_CONTRACT_AGREEMENT_NO_DUPLICATE", 13725, "核算公司和合同协议号不可重复");
    public static final ApiError LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED = new ApiError("LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED", 13648, "无法识别导入模板，请检查配置是否正确");
    public static final ApiError LOGISTICS_RECON_EXCEL_HEAD_NOT_FOUND = new ApiError("LOGISTICS_RECON_EXCEL_HEAD_NOT_FOUND", 13649, "未读取到 Excel 表头");
    public static final ApiError LOGISTICS_RECON_SAVE_FAILED = new ApiError("LOGISTICS_RECON_SAVE_FAILED", 13650, "物流商对账单保存失败");
    public static final ApiError LOGISTICS_RECON_PREPROCESS_IMPORT_NOT_READY = new ApiError("LOGISTICS_RECON_PREPROCESS_IMPORT_NOT_READY", 13651, "预处理导入功能暂未开放，请稍后再试");
    public static final ApiError LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN = new ApiError("LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN", 13652, "对账单导入中，暂不允许切换校验状态");
    public static final ApiError LOGISTICS_RECON_CHECK_STATUS_INVALID = new ApiError("LOGISTICS_RECON_CHECK_STATUS_INVALID", 13653, "非法的校验状态目标值");
    public static final ApiError LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN = new ApiError("LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN", 13654, "存在非未匹配费用项，不允许回退到待确认");
    public static final ApiError LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH = new ApiError("LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH", 13655, "仅已确认的对账单允许触发合并匹配");
    public static final ApiError LOGISTICS_RECON_RECONCILIATION_STATUS_INVALID = new ApiError("LOGISTICS_RECON_RECONCILIATION_STATUS_INVALID", 13656, "仅支持更新为待确认或账单确认");
    public static final ApiError LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_BILL_CONFIRM = new ApiError("LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_BILL_CONFIRM", 13657, "仅已确认的对账单允许执行账单确认");
    public static final ApiError LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND = new ApiError("LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND", 13658, "未找到已匹配的物流费用单");
    public static final ApiError LOGISTICS_RECON_CONFIRMED_DELETE_FORBIDDEN = new ApiError("LOGISTICS_RECON_CONFIRMED_DELETE_FORBIDDEN", 13659, "已确认的对账单不允许删除");
    public static final ApiError LOGISTICS_RECON_MANUAL_MATCH_NOT_READY = new ApiError("LOGISTICS_RECON_MANUAL_MATCH_NOT_READY", 13660, "手动匹配功能暂未开放，请稍后再试");
    public static final ApiError LOGISTICS_RECON_ADD_BILL_COST_NOT_READY = new ApiError("LOGISTICS_RECON_ADD_BILL_COST_NOT_READY", 13661, "新增费用单功能暂未开放，请稍后再试");
    public static final ApiError LOGISTICS_RECON_IMPORT_MATCH_NOT_READY = new ApiError("LOGISTICS_RECON_IMPORT_MATCH_NOT_READY", 13662, "导入匹配功能暂未开放，请稍后再试");
    public static final ApiError LOGISTICS_RECON_MATCH_NOT_READY = new ApiError("LOGISTICS_RECON_MATCH_NOT_READY", 13663, "合并匹配功能暂未开放，请稍后再试");
    public static final ApiError LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN = new ApiError("LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN", 13664, "存在匹配中的费用项，请等待匹配完成后再确认");
    public static final ApiError LOGISTICS_RECON_CONFIRM_COST_UPDATE_MISMATCH = new ApiError("LOGISTICS_RECON_CONFIRM_COST_UPDATE_MISMATCH", 13665, "账单确认失败：部分物流费用单状态未更新，请检查后重试");
    public static final ApiError LOGISTICS_RECON_CONFIRM_PARTIAL_FAILURE = new ApiError("LOGISTICS_RECON_CONFIRM_PARTIAL_FAILURE", 13666, "账单确认失败：第{0}批处理异常，前序批次可能已提交，请勿重复操作");
    public static final ApiError LOGISTICS_RECON_MATCH_POOL_BUSY = new ApiError("LOGISTICS_RECON_MATCH_POOL_BUSY", 13667, "匹配任务队列已满，请稍后重试");
    public static final ApiError LOGISTICS_RECON_IMPORT_FAILED_CHECK_STATUS_FORBIDDEN = new ApiError("LOGISTICS_RECON_IMPORT_FAILED_CHECK_STATUS_FORBIDDEN", 13668, "导入失败的对账单须重新导入后再确认");
    public static final ApiError LOGISTICS_RECON_IMPORTING_DUPLICATE = new ApiError("LOGISTICS_RECON_IMPORTING_DUPLICATE", 13669, "相同对账维度（月份+物流商+Sheet）正在导入，不允许重复导入");
    public static final ApiError LOGISTICS_RECON_IMPORT_MAIN_NOT_FOUND = new ApiError("LOGISTICS_RECON_IMPORT_MAIN_NOT_FOUND", 13670, "导入任务关联的对账单不存在或已失效，请重新提交导入");
    public static final ApiError LOGISTICS_RECON_CHECK_STATUS_NO_CHANGE = new ApiError("LOGISTICS_RECON_CHECK_STATUS_NO_CHANGE", 13671, "校验状态与当前状态相同，无需变更");
    public static final ApiError LOGISTICS_RECON_ONLY_PENDING_ALLOW_CHECK_CONFIRM = new ApiError("LOGISTICS_RECON_ONLY_PENDING_ALLOW_CHECK_CONFIRM", 13672, "仅待确认的对账单允许切换为已确认");
    public static final ApiError LOGISTICS_RECON_BILL_CONFIRM_ALREADY_TO_BE_CONFIRM = new ApiError("LOGISTICS_RECON_BILL_CONFIRM_ALREADY_TO_BE_CONFIRM", 13673, "当前关联数据已是待确认，无需更新");
    public static final ApiError LOGISTICS_RECON_BILL_CONFIRM_ALREADY_CONFIRMED = new ApiError("LOGISTICS_RECON_BILL_CONFIRM_ALREADY_CONFIRMED", 13674, "当前关联数据已是账单确认，无需更新");
    public static final ApiError LOGISTICS_RECON_BILL_CONFIRM_NO_ELIGIBLE = new ApiError("LOGISTICS_RECON_BILL_CONFIRM_NO_ELIGIBLE", 13675, "没有可确认的已匹配待确认数据");
    public static final ApiError LOGISTICS_RECON_BILL_REVERT_NO_ELIGIBLE = new ApiError("LOGISTICS_RECON_BILL_REVERT_NO_ELIGIBLE", 13676, "没有可回退为待确认的已确认数据");
    public static final ApiError LOGISTICS_RECON_BILL_REVERT_COST_STATUS_FORBIDDEN = new ApiError("LOGISTICS_RECON_BILL_REVERT_COST_STATUS_FORBIDDEN", 13677, "存在已确认关联数据，但物流费用核算状态非待生成或支付状态非未支付（待付款/待退款），无法回退为待确认");
    public static final ApiError LOGISTICS_SALES_PLATFORM_REQUIRED = new ApiError("LOGISTICS_SALES_PLATFORM_REQUIRED", 13649, "销售平台不能为空");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_AUTH_INFO_EMPTY = new ApiError("LOGISTICS_AIYA_CHANNEL_AUTH_INFO_EMPTY", 13730, "AIYA授权信息为空");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_TOKEN_SECRET_MISSING = new ApiError("LOGISTICS_AIYA_CHANNEL_TOKEN_SECRET_MISSING", 13731, "AIYA授权信息缺失partnerId(或appKey)/customerCode/appSecret");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_QUERY_ERROR = new ApiError("LOGISTICS_AIYA_CHANNEL_QUERY_ERROR", 13732, "AIYA查询派送渠道异常");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_RESPONSE_EMPTY = new ApiError("LOGISTICS_AIYA_CHANNEL_RESPONSE_EMPTY", 13733, "AIYA查询派送渠道接口返回为空");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_QUERY_FAILED = new ApiError("LOGISTICS_AIYA_CHANNEL_QUERY_FAILED", 13734, "AIYA查询派送渠道失败: {0}");
    public static final ApiError LOGISTICS_AIYA_CHANNEL_WAREHOUSE_EMPTY = new ApiError("LOGISTICS_AIYA_CHANNEL_WAREHOUSE_EMPTY", 13735, "AIYA查询仓库列表为空，无法同步派送渠道");
    public static final ApiError LOGISTICS_AIYA_SERVICE_NOT_OPEN = new ApiError("LOGISTICS_AIYA_SERVICE_NOT_OPEN", 13736, "AIYA物流服务功能暂未开放");

    static ApiError[] values() {
        return new ApiError[]{
                FIRST_MILE_FBA_SHIPMENT_NOT_EXIST_BILL,
                FIRST_MILE_SHIPMENT_NOT_FOUND,
                FIRST_MILE_SHIPMENT_DELETE_ALLOWED_PENDING_ONLY,
                FIRST_MILE_SHIPMENT_DETAIL_NOT_EXIST,
                FIRST_MILE_SHIPMENT_NOT_EXIST,
                FIRST_MILE_SHIPMENT_SKU_NOT_MAPPED,
                FIRST_MILE_SHIPMENT_CONTAIN_COMBINATION_REQUIRE_MACHINE,
                FIRST_MILE_SHIPMENT_INVENTORY_INSUFFICIENT,
                FIRST_MILE_SHIPMENT_RECEIVE_EXIST_REVERSE_FORBIDDEN,
                FIRST_MILE_SHIPMENT_STATUS_FINISH_ONLY,
                FIRST_MILE_SHIPMENT_ALREADY_PUSHED_NOT_DELETE,
                FIRST_MILE_SHIPMENT_STATUS_CHECK_NOT_DELETE,
                FIRST_MILE_SHIPMENT_ERROR,
                FIRST_MILE_SHIPMENT_PLAN_NOT_EXIST,
                FIRST_MILE_SHIPMENT_DETAIL_NOT_DISAPPROVE,
                FIRST_MILE_SHIPMENT_REQ_NOT_DISAPPROVE,
                FIRST_MILE_SHIPMENT_QTY_EXCEED_DECLARE_QTY,
                FIRST_MILE_SHIPMENT_REQ_NOT_FOUND,
                FIRST_MILE_SHIPMENT_REQ_DETAIL_NOT_FOUND,
                FIRST_MILE_SHIPMENT_NOTICE_DETAIL_NOT_FOUND,
                FIRST_MILE_SHIPMENT_WAIT_HANDLE_ONLY,
                FIRST_MILE_SHIPMENT_HANDLE_ING_FINISH_ONLY,
                FIRST_MILE_SHIPMENT_ONLY_FOR_OVERSEAS_WAREHOUSE,
                FIRST_MILE_SHIPMENT_APPROVE_ONLY_CAN_PUSH_OVERSEAS_INBOUND,
                FIRST_MILE_SHIPMENT_HANDLE_PRINT_PICKING_ALLOWED,
                FIRST_MILE_SHIPMENT_PACKING_NOT_COMPLETED_CANNOT_GENERATE_INBOUND,
                FIRST_MILE_SHIPMENT_FINANCE_COST_ALLOCATION_REVERSE_FORBIDDEN,
                FIRST_MILE_SHIPMENT_WAREHOUSE_REQUIRED,
                FIRST_MILE_SHIPMENT_DELIVERY_GENERATE_FAIL,
                FIRST_MILE_SHIPMENT_AWD_OUTSTOCK_NOT_EXIST,
                FIRST_MILE_SHIPMENT_GENERATE_NEED_BILL_DATE,
                FIRST_MILE_COST_ALLOCATION_ORG_ID_REQUIRED,
                FIRST_MILE_SHIPMENT_DEST_COUNTRY_INCONSISTENT,
                FIRST_MILE_SHIPMENT_DEST_COUNTRY_NOT_MAINTAINED,
                FIRST_MILE_SHIPMENT_SAVE_FAILED,
                FIRST_MILE_SHIPMENT_DECLARE_SOURCE_NOT_FOUND,
                FIRST_MILE_SHIPMENT_PACKING_TASK_NOT_GENERATED,
                FIRST_MILE_SHIPMENT_PACKING_WEIGHT_REQUIRED_FOR_APPROVE,
                FIRST_MILE_SHIPMENT_AVAILABLE_INVENTORY_INSUFFICIENT,
                FIRST_MILE_SHIPMENT_MACHINE_EXISTS_DELETE_FORBIDDEN,
                FIRST_MILE_SHIPMENT_INBOUND_EXISTS_DELETE_FORBIDDEN,
                FIRST_MILE_SHIPMENT_THIRD_WAREHOUSE_APPROVE_PUSH_FAILED,
                FIRST_MILE_SHIPMENT_LOGISTICS_AUTO_GENERATE_FAILED,
                FIRST_MILE_SHIPMENT_WORKFLOW_START_FAILED,
                FIRST_MILE_SHIPMENT_DEST_WAREHOUSE_REQUIRED,
                FIRST_MILE_DELIVERY_DECLARE_ALREADY_GENERATED,
                LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_RECONCILIATION_MONTH,
                LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL,
                LOGISTICS_PDF_MERGE_ERROR,
                LOGISTICS_PDF_MERGE_SKU_BARCODE_ERROR,
                LOGISTICS_PDF_SO_MERGE_ERROR,
                LOGISTICS_NO_TRACKING_NUMBER_CANNOT_MANUAL_SHIP,
                LOGISTICS_ALREADY_PACKAGE_TRANSFER_NOT_INTERCEPT,
                LOGISTICS_HANDLE_STATUS_ALREADY_HANDLED_OR_CANCEL_NOT,
                LOGISTICS_UPLOAD_SUCCESS_NOT_DELETE,
                LOGISTICS_DELIVERY_INTERCEPT_READY_PACKAGED,
                LOGISTICS_PACKING_REF_ORDER_APPROVED_FORBIDDEN,
                LOGISTICS_PACKING_TASK_NOT_FOUND,
                LOGISTICS_SYNC_ADDRESS_NOT_EDITABLE,
                LOGISTICS_SYNC_ADDRESS_NOT_DELETABLE,
                LOGISTICS_PACKING_SPEC_NOT_FOUND,
                LOGISTICS_PACKING_RECORD_NOT_FOUND,
                LOGISTICS_PACKING_FNSKU_QTY_EXCEEDS_UNPACKED,
                LOGISTICS_PACKING_SKU_QTY_EXCEEDS_BOX,
                LOGISTICS_PACKING_SKU_NOT_IN_ASSOCIATED_ORDER,
                LOGISTICS_PACKING_SKU_NOT_IN_BOX,
                LOGISTICS_PACKING_ASSOCIATED_ORDER_APPROVED_EDIT_DELETE_FORBIDDEN,
                LOGISTICS_PACKING_DELIVERY_CHECK_FORBIDDEN,
                LOGISTICS_PACKING_SKU_FNSKU_QTY_EXCEEDS_DELIVERY,
                LOGISTICS_PACKING_TOTAL_QTY_EXCEEDS_DELIVERY,
                LOGISTICS_PACKING_PICKLIST_REQUIRED,
                LOGISTICS_PACKING_SELECT_PICKLIST_REQUIRED,
                LOGISTICS_ORDER_EXISTS_REVERSE_FORBIDDEN,
                LOGISTICS_DECLARE_BILL_EXISTS_REVERSE_FORBIDDEN,
                LOGISTICS_FNSKU_LABEL_PRINT_FAILED,
                LOGISTICS_CUSTOMER_SKU_LABEL_PRINT_FAILED,
                LOGISTICS_PACKING_NOT_COMPLETED_DECLARATION_FORBIDDEN,
                LOGISTICS_FIRST_MILE_ORDER_EXISTS_NOT_DEL,
                LOGISTICS_DECLARE_BILL_EXISTS_NOT_DEL,
                LOGISTICS_CHANNEL_BLACKLIST,
                LOGISTICS_CHANNEL_COUNTRY_BLACKLIST,
                LOGISTICS_CANCEL_NOT_SUPPORTED,
                LOGISTICS_CANCEL_FAILED,
                LOGISTICS_PLATFORM_WAREHOUSE_NOT_INTERCEPT,
                LOGISTICS_CHANNEL_REQUIRED_FOR_CANCEL,
                LOGISTICS_NOT_INTERCEPTED_CANNOT_CANCEL,
                LOGISTICS_INTERCEPT_STATUS_INVALID,
                LOGISTICS_INTERCEPT_PROCESSING_FORBIDDEN_CANCEL,
                LOGISTICS_DECLARE_INFO_NOT_FOUND,
                LOGISTICS_DECLARE_SKU_NOT_FOUND,
                LOGISTICS_DECLARE_CN_NAME_REQUIRED,
                LOGISTICS_DECLARE_EN_NAME_REQUIRED,
                LOGISTICS_DECLARE_PRICE_REQUIRED,
                LOGISTICS_DECLARE_CURRENCY_REQUIRED,
                LOGISTICS_DECLARE_CURRENCY_SYMBOL_REQUIRED,
                LOGISTICS_DECLARE_WEIGHT_REQUIRED,
                LOGISTICS_DECLARE_CUSTOMS_INFO_REQUIRED,
                LOGISTICS_SHIPPING_TEMPLATE_NOT_FOUND,
                LOGISTICS_SHIPPING_DEST_COUNTRY_REQUIRED,
                LOGISTICS_SHIPPING_REGION_REQUIRED,
                LOGISTICS_SHIPPING_CITY_REQUIRED,
                LOGISTICS_SHIPPING_WAREHOUSE_REQUIRED,
                LOGISTICS_FIRST_WEIGHT_REQUIRED,
                LOGISTICS_FIRST_WEIGHT_COST_REQUIRED,
                LOGISTICS_ADDITIONAL_UNIT_WEIGHT_REQUIRED,
                LOGISTICS_ADDITIONAL_UNIT_PRICE_REQUIRED,
                LOGISTICS_SHIPPING_OTHER_COST_NOT_FOUND,
                LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DISABLED_FORBIDDEN,
                LOGISTICS_SHIPPING_TEMPLATE_CHANNEL_REF_DELETE_FORBIDDEN,
                LOGISTICS_SHIPPING_TEMPLATE_ALREADY_EXISTS,
                LOGISTICS_TEMPLATE_RULE_WEIGHT_COUNTRY_OVERLAP,
                LOGISTICS_TEMPLATE_RULE_WEIGHT_REGION_OVERLAP,
                LOGISTICS_TEMPLATE_RULE_WEIGHT_WAREHOUSE_OVERLAP,
                LOGISTICS_SHIPPING_RULE_NOT_FOUND,
                LOGISTICS_WEIGHT_OUT_OF_RANGE,
                LOGISTICS_OTHER_COST_SETTING_NOT_FOUND,
                LOGISTICS_ADDRESS_NAME_ALREADY_EXISTS,
                LOGISTICS_UNIT_PRICE_REQUIRED,
                LOGISTICS_CANCEL_AUTH_NOT_ALLOWED,
                LOGISTICS_SYNC_FORBIDDEN_NOT_AUTHORIZED,
                LOGISTICS_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN,
                LOGISTICS_CHANNEL_ADDRESS_REF_DELETE_FORBIDDEN,
                LOGISTICS_CHANNEL_NOT_FOUND,
                LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY,
                LOGISTICS_SALES_CHANNEL_NOT_CONFIGURED,
                LOGISTICS_PRINT_WAYBILL_FAILED,
                LOGISTICS_CHANNEL_QUOTE_REF_DELETE_FORBIDDEN,
                LOGISTICS_SELF_SHIP_BILL_STATUS_CHANGE_FORBIDDEN,
                LOGISTICS_CHANNEL_ALREADY_USED,
                LOGISTICS_SAILING_CONFIG_ALREADY_EXISTS,
                LOGISTICS_COST_NAME_ALREADY_EXISTS,
                LOGISTICS_WAREHOUSE_MAPPING_ALREADY_EXISTS,
                LOGISTICS_DECLARE_RECONCILIATION_NOT_FOUND,
                LOGISTICS_DECLARE_RECONCILIATION_DETAIL_NOT_FOUND,
                LOGISTICS_DECLARE_RECONCILIATION_SUPPLIER_MISMATCH,
                LOGISTICS_TRANSFER_SUPPLIER_NOT_FOUND_NOT_PACKAGE,
                LOGISTICS_ORDER_VOIDED_NOT_PACKAGE,
                LOGISTICS_TRANSFER_ORDER_INVALID_NOT_PACKAGE,
                LOGISTICS_ORDER_INTERCEPTED_NOT_PACKAGE,
                LOGISTICS_CHANNEL_AUTH_INFO_NOT_FOUND,
                LOGISTICS_DELIVERY_SUPPLIER_DUPLICATE,
                LOGISTICS_TRANSFER_SUPPLIER_DUPLICATE,
                LOGISTICS_GENERATE_TIME_AFTER_DEADLINE_FORBIDDEN,
                LOGISTICS_TRACK_STATUS_SYSTEM_MANAGED_NOT_EDITABLE,
                LOGISTICS_TRANSFER_SUPPLIER_REF_DELETE_FORBIDDEN,
                LOGISTICS_PRODUCT_NOT_REGISTERED,
                LOGISTICS_CHANNEL_CHANGE_FORBIDDEN_NOT_REGISTERED,
                LOGISTICS_TRANSFER_CHANNEL_EXIST_ENABLED_DISABLE_FORBIDDEN,
                LOGISTICS_PACKAGE_DIMENSION_REQUIRED,
                LOGISTICS_PRINT_WAYBILL_NOT_SUPPORTED,
                LOGISTICS_PRINT_ALLOCATE_CARGO_NOT_SUPPORTED,
                LOGISTICS_PRINT_SETTING_NOT_FOUND,
                LOGISTICS_COST_CONFIG_NOT_FOUND,
                LOGISTICS_CHANNEL_WAREHOUSE_REQUIRED,
                LOGISTICS_CONFIG_NOT_FOUND,
                LOGISTICS_CALL_THIRD_PLATFORM_ERROR,
                LOGISTICS_LABEL_TYPE_REQUIRED,
                LOGISTICS_LARGE_TABLE_EXISTS,
                LOGISTICS_LARGE_ESTIMATED_EXISTS,
                LOGISTICS_SMALL_BAG_NOT_CONFIRMED,
                LOGISTICS_SELF_SHIP_FEE_NOT_FOUND,
                LOGISTICS_ACTUAL_EXISTS_CANNOT_PUSH,
                LOGISTICS_MAPPING_NOT_NULL,
                LOGISTICS_SUPPLIER_NOT_FOUND,
                LOGISTICS_SUPPLIER_NOT_EXIST,
                LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND,
                LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND,
                LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND,
                LOGISTICS_SUPPLIER_NAME_NOT_FOUND,
                LOGISTICS_BILL_COST_IMPORT_RECORD_UNIQUE_KEY_ERROR,
                LOGISTICS_ASYNC_TASK_CREATE_ERROR,
                LOGISTICS_PENDING_COST_NOT_FOUND,
                LOGISTICS_SELECT_AT_LEAST_ONE,
                LOGISTICS_BILL_FIELD_DUPLICATE_NOT_ALLOWED,
                LOGISTICS_BILL_DETAIL_FIELD_REQUIRED,
                LOGISTICS_SMALL_BAG_NOT_CAN_Allocate,
                LOGISTICS_BILL_COST_IMPORT_RECORD_HEAD_NOTFOUND,
                LOGISTICS_BILL_UNIQUE_FIELD_NOT_ALLOWED,
                LOGISTICS_THIRD_CHANNEL_PUSH_TYPE_REQUIRED,
                LOGISTICS_THIRD_CHANNEL_SAVE_FAILED,
                LOGISTICS_THIRD_CHANNEL_NOT_FOUND,
                LOGISTICS_THIRD_CHANNEL_IN_USE_DELETE_FORBIDDEN,
                LOGISTICS_THIRD_CHANNEL_STATUS_UNCHANGED,
                LOGISTICS_THIRD_CHANNEL_DUPLICATE,
                LOGISTICS_THIRD_CHANNEL_SUPPLIER_NOT_FOUND,
                LOGISTICS_THIRD_CHANNEL_CHANNEL_NOT_FOUND,
                LOGISTICS_THIRD_CHANNEL_QUERY_PROVIDER_NOT_FOUND,
                LOGISTICS_THIRD_CHANNEL_PUSH_MOBILE_IMMUTABLE,
                LOGISTICS_THIRD_CHANNEL_MOBILE_REQUIRED,
                LOGISTICS_THIRD_CHANNEL_SHOP_ID_REQUIRED,
                LOGISTICS_THIRD_CHANNEL_PLATFORM_REQUIRED,
                LOGISTICS_THIRD_CHANNEL_DETAIL_NOT_REQUIRED,
                LOGISTICS_THIRD_CHANNEL_QUERY_SUPPLIER_NAME_REQUIRED,
                LOGISTICS_ORDER_NOT_CANCEL,
                LOGISTICS_ORDER_CANNOT_EDIT,
                LOGISTICS_CHANNEL_CODE_EMPTY,
                LOGISTICS_DECLARE_BILL_EXISTS_NOT_SOURCE,
                LOGISTICS_DECLARE_DETAIL_MID_DETAIL_MATCH_FAILED,
                LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND,
                LOGISTICS_PRODUCT_LOGISTIC_DECLARE_INFO_INCOMPLETE,
                LOGISTICS_PRODUCT_LOGISTIC_DECLARE_PRICE_REQUIRED,
                LOGISTICS_COMBO_DECLARE_CHILD_EMPTY,
                LOGISTICS_COMBO_DECLARE_CHILD_QTY_EMPTY,
                LOGISTICS_DECLARE_DETAIL_MID_AUTO_GENERATE_FAILED,
                LOGISTICS_DECLARE_BILL_AUTO_GENERATE_FAILED,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SENDER_TYPE_CONFLICT,
                LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED,
                LOGISTICS_DECLARE_STATUS_UPDATE_FORBIDDEN,
                LOGISTICS_DECLARE_STATUS_CONFIRM_DATE_REQUIRED,
                LOGISTICS_DECLARE_STATUS_CONFIRM_USER_REQUIRED,
                LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED,
                LOGISTICS_DECLARE_STATUS_DETAIL_INCONSISTENT,
                LOGISTICS_DECLARE_BILL_TYPE_MISMATCH,
                LOGISTICS_DECLARE_STATUS_INVALID_TARGET,
                LOGISTICS_DECLARE_MERGE_RECEIVER_TYPE_DIFF,
                LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_NOT_FOUND,
                LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_FAILED,
                LOGISTICS_DECLARE_SENDER_REQUIRED,
                LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE,
                LOGISTICS_DECLARE_RULE_CONDITION_DUPLICATE,
                LOGISTICS_DECLARE_RULE_SAVE_FAILED,
                LOGISTICS_DECLARE_GENERATABLE_DELIVERY_NOT_FOUND,
                LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED,
                LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND,
                LOGISTICS_DECLARE_BILL_SAVE_FAILED,
                LOGISTICS_DECLARE_WAIT_STATUS_REQUIRED_FOR_EDIT,
                LOGISTICS_DECLARE_DETAIL_SAVE_FAILED,
                LOGISTICS_DECLARE_DELIVERY_NOT_FOUND,
                LOGISTICS_DECLARE_SO_OUT_NOT_FOUND,
                LOGISTICS_DECLARE_STATUS_INVALID,
                LOGISTICS_DECLARE_SELECTED_SKU_REQUIRED,
                LOGISTICS_DECLARE_SOURCE_HEADER_NOT_FOUND,
                LOGISTICS_DECLARE_GENERATABLE_SO_DELIVERY_NOT_FOUND,
                LOGISTICS_DECLARE_BATCH_UPDATE_FAILED,
                LOGISTICS_DECLARE_FIELD_DATE_FORMAT_INVALID,
                LOGISTICS_DECLARE_FIELD_NUMBER_FORMAT_INVALID,
                LOGISTICS_DECLARE_FM_SPLIT_VIEW_FORBIDDEN,
                LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED,
                LOGISTICS_DECLARE_SPLIT_SINGLE_BILL_REQUIRED,
                LOGISTICS_DECLARE_FM_SPLIT_ADD_FORBIDDEN,
                LOGISTICS_DECLARE_B2B_SPLIT_ADD_FORBIDDEN,
                LOGISTICS_DECLARE_MERGE_WAIT_STATUS_REQUIRED,
                LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE,
                LOGISTICS_DECLARE_MERGE_SKU_LIMIT_EXCEEDED,
                LOGISTICS_DECLARE_MERGE_COUNTRY_MISMATCH,
                LOGISTICS_DECLARE_DETAIL_SOURCE_REQUIRED,
                LOGISTICS_DECLARE_DETAIL_SOURCE_DUPLICATE,
                LOGISTICS_DECLARE_DETAIL_QTY_MISMATCH,
                LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE,
                LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED,
                LOGISTICS_DECLARE_SOURCE_GENERATED,
                LOGISTICS_DECLARE_SOURCE_BOX_IMMUTABLE,
                LOGISTICS_DECLARE_SOURCE_QTY_IMMUTABLE,
                LOGISTICS_DECLARE_DUPLICATE_SKU_ROW,
                LOGISTICS_DECLARE_DETAIL_SOURCE_VALUE_MISMATCH,
                LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED,
                LOGISTICS_DECLARE_DETAIL_FIELD_POSITIVE_REQUIRED,
                LOGISTICS_DECLARE_REPLACE_TYPE_MISMATCH,
                LOGISTICS_DECLARE_REPLACE_WAIT_STATUS_REQUIRED,
                LOGISTICS_DECLARE_BOX_SINGLE_BILL_REQUIRED,
                LOGISTICS_DECLARE_B2B_SPLIT_VIEW_FORBIDDEN,
                LOGISTICS_DECLARE_COMBO_CHILD_NOT_FULL_SELECTED,
                LOGISTICS_DECLARE_BOM_HISTORY_NOT_FOUND,
                LOGISTICS_DECLARE_LATEST_PRODUCT_LOGISTIC_NOT_FOUND,
                LOGISTICS_PACKING_DELIVERY_CHECK_DECLARE_STATUS,
                LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED,
                LOGISTICS_DECLARE_DEST_COUNTRY_CN_NOT_GENERATE,
                LOGISTICS_DECLARE_BOX_NOT_FULL_SELECTED,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RECEIVER_TYPE_CONFLICT,
                LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND_FOR_SOURCE,
                LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED_BATCH,
                LOGISTICS_DECLARE_SPLIT_DETAIL_MISSING,
                LOGISTICS_DECLARE_SOURCE_STATUS_SYNC_FAILED,
                LOGISTICS_DECLARE_DETAIL_MID_SAVE_FAILED,
                LOGISTICS_DECLARE_DETAIL_MID_NOT_EXIST,
                LOGISTICS_DECLARE_DETAIL_MID_UPDATE_FAILED,
                LOGISTICS_DECLARE_B2B_SO_DETAIL_NOT_FOUND,
                LOGISTICS_DECLARE_B2B_CUSTOMER_RECEIVER_NOT_FOUND,
                LOGISTICS_DECLARE_RULE_TYPE_INVALID,
                LOGISTICS_DECLARE_RULE_TYPE_MISMATCH,
                LOGISTICS_DECLARE_RULE_CROSS_TYPE_UPDATE_FORBIDDEN,
                LOGISTICS_DECLARE_RULE_ACCOUNTING_COMPANY_LOAD_FAILED,
                LOGISTICS_CONTRACT_AGREEMENT_NO_CONFIG_REQUIRED,
                LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_REQUIRED,
                LOGISTICS_CONTRACT_AGREEMENT_NO_REQUIRED,
                LOGISTICS_CONTRACT_AGREEMENT_NO_LETTERS_ONLY,
                LOGISTICS_CONTRACT_AGREEMENT_NO_COMPANY_INVALID,
                LOGISTICS_CONTRACT_AGREEMENT_NO_DUPLICATE,
                LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED,
                LOGISTICS_RECON_EXCEL_HEAD_NOT_FOUND,
                LOGISTICS_RECON_SAVE_FAILED,
                LOGISTICS_RECON_PREPROCESS_IMPORT_NOT_READY,
                LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN,
                LOGISTICS_RECON_CHECK_STATUS_INVALID,
                LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN,
                LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH,
                LOGISTICS_RECON_RECONCILIATION_STATUS_INVALID,
                LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_BILL_CONFIRM,
                LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND,
                LOGISTICS_RECON_CONFIRMED_DELETE_FORBIDDEN,
                LOGISTICS_RECON_MANUAL_MATCH_NOT_READY,
                LOGISTICS_RECON_ADD_BILL_COST_NOT_READY,
                LOGISTICS_RECON_IMPORT_MATCH_NOT_READY,
                LOGISTICS_RECON_MATCH_NOT_READY,
                LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN,
                LOGISTICS_RECON_CONFIRM_COST_UPDATE_MISMATCH,
                LOGISTICS_RECON_CONFIRM_PARTIAL_FAILURE,
                LOGISTICS_RECON_MATCH_POOL_BUSY,
                LOGISTICS_RECON_IMPORT_FAILED_CHECK_STATUS_FORBIDDEN,
                LOGISTICS_RECON_IMPORTING_DUPLICATE,
                LOGISTICS_RECON_IMPORT_MAIN_NOT_FOUND,
                LOGISTICS_RECON_CHECK_STATUS_NO_CHANGE,
                LOGISTICS_RECON_ONLY_PENDING_ALLOW_CHECK_CONFIRM,
                LOGISTICS_RECON_BILL_CONFIRM_ALREADY_TO_BE_CONFIRM,
                LOGISTICS_RECON_BILL_CONFIRM_ALREADY_CONFIRMED,
                LOGISTICS_RECON_BILL_CONFIRM_NO_ELIGIBLE,
                LOGISTICS_RECON_BILL_REVERT_NO_ELIGIBLE,
                LOGISTICS_RECON_BILL_REVERT_COST_STATUS_FORBIDDEN,
                LOGISTICS_SALES_PLATFORM_REQUIRED,
                LOGISTICS_AIYA_CHANNEL_AUTH_INFO_EMPTY,
                LOGISTICS_AIYA_CHANNEL_TOKEN_SECRET_MISSING,
                LOGISTICS_AIYA_CHANNEL_QUERY_ERROR,
                LOGISTICS_AIYA_CHANNEL_RESPONSE_EMPTY,
                LOGISTICS_AIYA_CHANNEL_QUERY_FAILED,
                LOGISTICS_AIYA_CHANNEL_WAREHOUSE_EMPTY,
                LOGISTICS_AIYA_SERVICE_NOT_OPEN,
        };
    }
}
