package com.common.core.enums;

/**
 * Oms service error constants.
 */
public final class ApiErrorOms {

    private ApiErrorOms() {
    }

    public static final ApiError CUSTOMER_ADDRESS_NOT_MATCH = new ApiError("CUSTOMER_ADDRESS_NOT_MATCH", 94108, "未匹配到客户地址，客户id：{0}，收货地址：{1}");
    public static final ApiError CUSTOMER_GROUP_REQUIRED = new ApiError("CUSTOMER_GROUP_REQUIRED", 13000, "客户分组不能为空");
    public static final ApiError CUSTOMER_GROUP_NAME_DUPLICATE = new ApiError("CUSTOMER_GROUP_NAME_DUPLICATE", 13001, "客户分组名不能重复");
    public static final ApiError CUSTOMER_GROUP_IN_USE_DELETE_FORBIDDEN = new ApiError("CUSTOMER_GROUP_IN_USE_DELETE_FORBIDDEN", 13002, "客户分组已被引用，无法删除");
    public static final ApiError CUSTOMER_DEFAULT_CONTACT_LIMIT = new ApiError("CUSTOMER_DEFAULT_CONTACT_LIMIT", 13003, "客户默认联系人最多只能设置一个");
    public static final ApiError CUSTOMER_DEFAULT_ADDRESS_LIMIT = new ApiError("CUSTOMER_DEFAULT_ADDRESS_LIMIT", 13004, "客户默认地址最多只能设置一个");
    public static final ApiError CUSTOMER_DEFAULT_BANK_LIMIT = new ApiError("CUSTOMER_DEFAULT_BANK_LIMIT", 13005, "客户默认开户行最多只能设置一个");
    public static final ApiError CUSTOMER_NOT_FOUND = new ApiError("CUSTOMER_NOT_FOUND", 13006, "客户不存在");
    public static final ApiError CUSTOMER_ADDRESS_IN_USE_DELETE_FORBIDDEN = new ApiError("CUSTOMER_ADDRESS_IN_USE_DELETE_FORBIDDEN", 13007, "客户地址已被销售订单引用，无法删除");
    public static final ApiError CUSTOMER_DISABLE_FORBIDDEN = new ApiError("CUSTOMER_DISABLE_FORBIDDEN", 13008, "客户已被使用，无法停用");
    public static final ApiError CUSTOMER_SKU_INTERVAL_OVERLAP = new ApiError("CUSTOMER_SKU_INTERVAL_OVERLAP", 13009, "客户SKU价格区间存在重叠，不允许提交");
    public static final ApiError CUSTOMER_NAME_DUPLICATE = new ApiError("CUSTOMER_NAME_DUPLICATE", 13010, "客户名称不能重复");
    public static final ApiError CUSTOMER_NAME_RELATE_SHOP_FORBIDDEN = new ApiError("CUSTOMER_NAME_RELATE_SHOP_FORBIDDEN", 13011, "客户已关联店铺，不允许修改客户名称");
    public static final ApiError SALES_DEMAND_NOT_FOUND = new ApiError("SALES_DEMAND_NOT_FOUND", 9000, "未找到备货申请单");
    public static final ApiError SALES_DEMAND_DETAIL_NOT_FOUND = new ApiError("SALES_DEMAND_DETAIL_NOT_FOUND", 9001, "未找到备货申请单明细");
    public static final ApiError SALES_DEMAND_SKU_QTY_EXCEEDS = new ApiError("SALES_DEMAND_SKU_QTY_EXCEEDS", 9002, "备货申请单SKU【{0}】数量不能超过【{1}】");
    public static final ApiError SALES_DEMAND_ALREADY_PUSHED_SUBCONTRACT_REVERSE_FORBIDDEN = new ApiError("SALES_DEMAND_ALREADY_PUSHED_SUBCONTRACT_REVERSE_FORBIDDEN", 9003, "所选采购申请单已下推委外订单，不支持反审核");
    public static final ApiError SHOP_NAME_EXISTS = new ApiError("SHOP_NAME_EXISTS", 8000, "店铺名称已存在");
    public static final ApiError SHOP_AUTH_SHIPMENT_ERROR = new ApiError("SHOP_AUTH_SHIPMENT_ERROR", 8001, "店铺未授权，无法拉取货件");
    public static final ApiError SHOP_NOT_AUTH_ERROR = new ApiError("SHOP_NOT_AUTH_ERROR", 8002, "店铺尚未授权，无法执行该操作");
    public static final ApiError SHOP_FBA_MARKETPLACE_DISABLED = new ApiError("SHOP_FBA_MARKETPLACE_DISABLED", 8003, "亚马逊店铺已被禁用:{0}");
    public static final ApiError SHOP_AUTHORIZE_CODE_REQUIRED = new ApiError("SHOP_AUTHORIZE_CODE_REQUIRED", 8004, "授权code不能为空");
    public static final ApiError SHOP_AUTHORIZE_FAILED = new ApiError("SHOP_AUTHORIZE_FAILED", 8005, "授权失败，原因：【{0}】");
    public static final ApiError SHOP_TOKEN_FETCH_FAILED = new ApiError("SHOP_TOKEN_FETCH_FAILED", 8006, "店铺【{0}】获取Token失败");
    public static final ApiError SHOP_ALREADY_AUTHORIZED = new ApiError("SHOP_ALREADY_AUTHORIZED", 8007, "店铺已完成授权");
    public static final ApiError SHOP_LISTING_NOT_FOUND = new ApiError("SHOP_LISTING_NOT_FOUND", 8008, "Listing信息不存在");
    public static final ApiError SHOP_AUTHORIZE_ERROR = new ApiError("SHOP_AUTHORIZE_ERROR", 8009, "授权的平台编码错误");
    public static final ApiError SHOP_WALMART_ID_REQUIRED = new ApiError("SHOP_WALMART_ID_REQUIRED", 8010, "沃尔玛授权店铺Id不能为空");
    public static final ApiError SHOP_WALMART_CLIENT_ID_REQUIRED = new ApiError("SHOP_WALMART_CLIENT_ID_REQUIRED", 8011, "沃尔玛授权ClientId不能为空");
    public static final ApiError SHOP_WALMART_CLIENT_SECRET_REQUIRED = new ApiError("SHOP_WALMART_CLIENT_SECRET_REQUIRED", 8012, "沃尔玛授权ClientSecret不能为空");
    public static final ApiError SHOP_AUTH_REQUIRED = new ApiError("SHOP_AUTH_REQUIRED", 8013, "指定店铺授权时，店铺不能为空");
    public static final ApiError SHOP_EXIST = new ApiError("SHOP_EXIST", 8014, "平台【{0}】下账号【{1}】已存在店铺");
    public static final ApiError SHOP_COUNTRY_EXIST = new ApiError("SHOP_COUNTRY_EXIST", 8015, "平台【{0}】账号【{1}】下国家【{2}】已存在店铺");
    public static final ApiError SHOP_NOT_FOUND = new ApiError("SHOP_NOT_FOUND", 8016, "店铺不存在");
    public static final ApiError SHOP_NOT_EXIST_NO_PERMISSION = new ApiError("SHOP_NOT_EXIST_NO_PERMISSION", 8017, "店铺不存在或当前用户无权限");
    public static final ApiError SHOP_PARAM_AUTHORIZE_FAILED = new ApiError("SHOP_PARAM_AUTHORIZE_FAILED", 8018, "店铺【{0}】授权失败，原因：【{1}】");
    public static final ApiError SHOP_DELETE_ONLY_DISABLED = new ApiError("SHOP_DELETE_ONLY_DISABLED", 8019, "仅禁用状态的店铺允许删除");
    public static final ApiError SHOP_INVOICE_NOT_BIND_COMPANY = new ApiError("SHOP_INVOICE_NOT_BIND_COMPANY", 8020, "店铺【{0}】未配置公司账号");
    public static final ApiError SHOP_COUNTRY_CODE_REQUIRED = new ApiError("SHOP_COUNTRY_CODE_REQUIRED", 8021, "店铺未配置国家，无法完成授权");
    public static final ApiError SHOP_TIKTOK_AUTHORIZED_SHOPS_EMPTY = new ApiError("SHOP_TIKTOK_AUTHORIZED_SHOPS_EMPTY", 8022, "TikTok授权失败：未获取到平台授权站点");
    public static final ApiError SHOP_TIKTOK_REGION_NOT_MATCH = new ApiError("SHOP_TIKTOK_REGION_NOT_MATCH", 8023, "TikTok授权失败：店铺国家[{0}]在平台授权站点中不存在，可用站点：{1}");
    public static final ApiError SO_DELIVERY_DETAIL_SKU_NOT_EXIST = new ApiError("SO_DELIVERY_DETAIL_SKU_NOT_EXIST", 10500, "SKU【{0}】在发货通知单中未找到");
    public static final ApiError SO_DELIVERY_NOTICE_NOT_EXIST = new ApiError("SO_DELIVERY_NOTICE_NOT_EXIST", 10501, "发货通知单不存在");
    public static final ApiError SO_DELIVERY_SALES_ORDER_PUSH_STOCK_APPLY_QTY_EXCEEDS = new ApiError("SO_DELIVERY_SALES_ORDER_PUSH_STOCK_APPLY_QTY_EXCEEDS", 10502, "销售订单【{0}】下推备货申请单SKU【{1}】数量不能超过【{2}】");
    public static final ApiError SO_RETURN_PUSH_ALLOWED_SOURCE_SALES_RETURN_RECEIPT_ONLY = new ApiError("SO_RETURN_PUSH_ALLOWED_SOURCE_SALES_RETURN_RECEIPT_ONLY", 10503, "只有来源是【销售退货签收单】的单据可以下推【销售退货入库单】");
    public static final ApiError SO_CLOSED_PRODUCT_EXISTS_CANNOT_PUSH = new ApiError("SO_CLOSED_PRODUCT_EXISTS_CANNOT_PUSH", 10504, "存在已关闭的产品,不能下推单据");
    public static final ApiError SO_OUTBOUND_NOT_FOUND = new ApiError("SO_OUTBOUND_NOT_FOUND", 10505, "销售出库单不存在");
    public static final ApiError SO_RETURN_RECEIPT_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("SO_RETURN_RECEIPT_ALREADY_PUSHED_REVERSE_FORBIDDEN", 10506, "已下推销售退货签收单，不能反审核");
    public static final ApiError SO_DEMAND_REQ_PUSHED_DELIVERY_PICKLIST_LOCKED = new ApiError("SO_DEMAND_REQ_PUSHED_DELIVERY_PICKLIST_LOCKED", 10507, "要货申请下推发货单后，关联的拣货单不允许修改和删除");
    public static final ApiError SO_NOTICE_PUSHED_OUTBOUND_PICKLIST_LOCKED = new ApiError("SO_NOTICE_PUSHED_OUTBOUND_PICKLIST_LOCKED", 10508, "销售通知单下推销售出库单后，拣货单不允许修改和删除");
    public static final ApiError SO_PICKLIST_NOT_FOUND_FOR_SO = new ApiError("SO_PICKLIST_NOT_FOUND_FOR_SO", 10509, "【{0}】未生成拣货单，不允许下推销售出库单");
    public static final ApiError SO_PICKLIST_EXISTS_FORBID_VOID_DELETE = new ApiError("SO_PICKLIST_EXISTS_FORBID_VOID_DELETE", 10510, "存在拣货单，不允许作废、删除或撤销该单据");
    public static final ApiError SO_OUTBOUND_QTY_EXCEEDS_ORDER = new ApiError("SO_OUTBOUND_QTY_EXCEEDS_ORDER", 10511, "SKU【{0}】的出库数量不能大于销售订单的销售数量");
    public static final ApiError SO_DEMAND_REQ_ALREADY_PUSHED_DELIVERY = new ApiError("SO_DEMAND_REQ_ALREADY_PUSHED_DELIVERY", 10512, "要货申请已生成发货单并完成处理，无需重复下推");
    public static final ApiError SO_NOT_APPROVED_PUSH_OUTBOUND_FORBIDDEN = new ApiError("SO_NOT_APPROVED_PUSH_OUTBOUND_FORBIDDEN", 10513, "销售订单未审核，不允许下推生成出库单");
    public static final ApiError SO_PICKLIST_ALREADY_EXISTS = new ApiError("SO_PICKLIST_ALREADY_EXISTS", 10514, "已存在拣货单【{0}】，不允许重复生成");
    public static final ApiError SO_PICKLIST_TOTAL_QTY_ZERO_FORBIDDEN = new ApiError("SO_PICKLIST_TOTAL_QTY_ZERO_FORBIDDEN", 10515, "拣货数量合计不能为0");
    public static final ApiError SO_PICKLIST_DETAIL_NOT_FOUND_FOR_SO = new ApiError("SO_PICKLIST_DETAIL_NOT_FOUND_FOR_SO", 10516, "未找到销售订单【{0}】对应的拣货明细");
    public static final ApiError SO_SKU_FULLY_ALLOCATED = new ApiError("SO_SKU_FULLY_ALLOCATED", 10517, "SKU【{0}】已全部完成分货");
    public static final ApiError SO_WAVE_GENERATED_SHORTAGE_AUTO = new ApiError("SO_WAVE_GENERATED_SHORTAGE_AUTO", 10518, "该波次已自动生成缺货记录，无需手动处理");
    public static final ApiError SO_ABNORMAL_ORDER_HANDLE_ALLOWED_ONLY = new ApiError("SO_ABNORMAL_ORDER_HANDLE_ALLOWED_ONLY", 10519, "只有异常单状态的发货单才允许执行异常处理操作");
    public static final ApiError SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN = new ApiError("SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN", 10520, "待处理或异常状态的发货单不支持自动发货");
    public static final ApiError SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN_FOR_SO = new ApiError("SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN_FOR_SO", 10521, "销售订单【{0}】对应的发货单存在待处理或异常数据，不支持自动发货");
    public static final ApiError SO_WAVE_GEN_ALLOWED_PENDING_NON_INTERCEPT = new ApiError("SO_WAVE_GEN_ALLOWED_PENDING_NON_INTERCEPT", 10522, "仅待处理且未处于拦截中的发货单允许生成波次");
    public static final ApiError SO_WAVE_NO_AND_SKU_REQUIRED = new ApiError("SO_WAVE_NO_AND_SKU_REQUIRED", 10523, "波次号和sku不能为空");
    public static final ApiError SO_WAVE_EXIST_MANAGED_AND_NORMAL_ORDER = new ApiError("SO_WAVE_EXIST_MANAGED_AND_NORMAL_ORDER", 10524, "存在托管订单与其他小包订单混合，请分开生成波次");
    public static final ApiError SO_WAVE_NO_AND_BASKET_REQUIRED = new ApiError("SO_WAVE_NO_AND_BASKET_REQUIRED", 10525, "波次号与篮号不能为空");
    public static final ApiError SO_WAVE_NO_REQUIRED = new ApiError("SO_WAVE_NO_REQUIRED", 10526, "波次号不能为空");
    public static final ApiError SO_WAVE_SKU_NOT_FOUND = new ApiError("SO_WAVE_SKU_NOT_FOUND", 10527, "当前波次中不存在该SKU");
    public static final ApiError SO_WAVE_SAME_WAREHOUSE_REQUIRED = new ApiError("SO_WAVE_SAME_WAREHOUSE_REQUIRED", 10528, "仅允许相同仓库的发货单生成同一波次");
    public static final ApiError SO_IN_AUTO_REPLENISH_PRINT_FORBIDDEN = new ApiError("SO_IN_AUTO_REPLENISH_PRINT_FORBIDDEN", 10529, "单据【{0}】处于异常波次缺货自动补货处理中，不允许打印");
    public static final ApiError SO_IN_PICK_OR_SUSPENDED_INTERCEPT_FORBIDDEN = new ApiError("SO_IN_PICK_OR_SUSPENDED_INTERCEPT_FORBIDDEN", 10530, "单据【{0}】所属波次处于拣货中或挂起状态，不允许执行拦截操作");
    public static final ApiError SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FAIL = new ApiError("SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FAIL", 10531, "待处理或异常取消发货的数据不支持拦截失败操作");
    public static final ApiError SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FORBIDDEN = new ApiError("SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FORBIDDEN", 10532, "异常单或取消发货的数据不支持拦截操作");
    public static final ApiError SO_SKU_NOT_PICKED_CANNOT_ALLOCATE = new ApiError("SO_SKU_NOT_PICKED_CANNOT_ALLOCATE", 10533, "SKU尚未完成拣货，不允许执行分货操作");
    public static final ApiError SO_NOTICE_PICK_QTY_EXCEEDS_DELIVERY = new ApiError("SO_NOTICE_PICK_QTY_EXCEEDS_DELIVERY", 10534, "发货通知单【{0}】的拣货数量不能大于发货数量");
    public static final ApiError SO_BUNDLE_PICK_QTY_RATIO_INVALID = new ApiError("SO_BUNDLE_PICK_QTY_RATIO_INVALID", 10535, "组合产品【{0}】的拣货数量与BOM用量比例不一致，无法修改");
    public static final ApiError SO_OUTBOUND_ALREADY_PUSHED = new ApiError("SO_OUTBOUND_ALREADY_PUSHED", 10536, "已下推销售出库单，不允许重复下推");
    public static final ApiError SO_DEMAND_REQ_COMPLETED_PICKLIST_LOCKED = new ApiError("SO_DEMAND_REQ_COMPLETED_PICKLIST_LOCKED", 10537, "要货申请完成后，不允许修改或删除拣货单");
    public static final ApiError SO_PICKLIST_GENERATED_TRANSFER_REVERSE_FORBIDDEN = new ApiError("SO_PICKLIST_GENERATED_TRANSFER_REVERSE_FORBIDDEN", 10538, "由拣货单生成的移仓单不支持反审核");
    public static final ApiError SO_NOTICE_ALREADY_PICKLIST_LOCKED = new ApiError("SO_NOTICE_ALREADY_PICKLIST_LOCKED", 10539, "已生成拣货单，不允许修改发货通知单");
    public static final ApiError SO_PICKLIST_PUSHED_DELIVERY_WH_MOVE_LOCKED = new ApiError("SO_PICKLIST_PUSHED_DELIVERY_WH_MOVE_LOCKED", 10540, "拣货单下推的发货单，不允许修改仓位移动单");
    public static final ApiError SO_THIRD_PARTY_ORDER_MODIFY_FORBIDDEN = new ApiError("SO_THIRD_PARTY_ORDER_MODIFY_FORBIDDEN", 10541, "第三方平台来源的单据不允许修改");
    public static final ApiError SO_NOTICE_APPROVED_PICKLIST_GEN_FORBIDDEN = new ApiError("SO_NOTICE_APPROVED_PICKLIST_GEN_FORBIDDEN", 10542, "发货通知单已审核，不允许继续生成拣货单");
    public static final ApiError SO_NOTICE_APPROVED_PICKLIST_MODIFY_DELETE_FORBIDDEN = new ApiError("SO_NOTICE_APPROVED_PICKLIST_MODIFY_DELETE_FORBIDDEN", 10543, "发货通知单已审核，不允许修改或删除拣货单");
    public static final ApiError SO_RETURN_INSTOCK_NOT_GENERATE = new ApiError("SO_RETURN_INSTOCK_NOT_GENERATE", 10544, "退货入库单【{0}】未审核通过，不支持继续下推");
    public static final ApiError SO_DELIVERY_STATUS_NOT_SUPPORT_MANUAL_SHIP_FLAG = new ApiError("SO_DELIVERY_STATUS_NOT_SUPPORT_MANUAL_SHIP_FLAG", 10545, "手动标发、已发货、取消发货状态的发货单不允许再次手动标发");
    public static final ApiError SO_DELIVERY_B2C_NOT_EXISTS = new ApiError("SO_DELIVERY_B2C_NOT_EXISTS", 10546, "b2c发货单不存在");
    public static final ApiError SO_DELIVERY_STATUS_NOT_ALLOW_MANUAL_DELIVERY = new ApiError("SO_DELIVERY_STATUS_NOT_ALLOW_MANUAL_DELIVERY", 10547, "待处理、已发货、异常单、取消发货状态的数据不允许手动发货");
    public static final ApiError SO_DELIVERY_PLATFORM_ERROR_MSG = new ApiError("SO_DELIVERY_PLATFORM_ERROR_MSG", 10548, "平台发货失败，错误信息【{0}】");
    public static final ApiError SO_RETURN_RECEIVE_SKU_NOT_EXIST = new ApiError("SO_RETURN_RECEIVE_SKU_NOT_EXIST", 10549, "sku【{0}】在退货签收单中不存在");
    public static final ApiError SO_PUSH_MACHINE = new ApiError("SO_PUSH_MACHINE", 10550, "销售订单【{0}】SKU【{1}】已下推加工单");
    public static final ApiError SO_APPROVE_ONLY_CAN_UPLOAD_PACKING = new ApiError("SO_APPROVE_ONLY_CAN_UPLOAD_PACKING", 10551, "待审核的数据才可以上传装箱数据");
    public static final ApiError SO_DELIVERY_ALREADY_PUSHED_NOT_UPDATE_MAPPING = new ApiError("SO_DELIVERY_ALREADY_PUSHED_NOT_UPDATE_MAPPING", 10552, "已下推发货单，不允许修改发货信息");
    public static final ApiError SO_B2C_DELIVERY_ALREADY_EXIST = new ApiError("SO_B2C_DELIVERY_ALREADY_EXIST", 10553, "订单【{0}】已生成发货单，不允许重复新增！");
    public static final ApiError SO_INTERCEPTED_STATUS_NOT_UPDATE = new ApiError("SO_INTERCEPTED_STATUS_NOT_UPDATE", 10554, "订单【{0}】已发起拦截并被冻结，禁止修改状态");
    public static final ApiError SO_FULLY_MANAGED_ORDER_NOT_NEED_MANUAL_SHIP = new ApiError("SO_FULLY_MANAGED_ORDER_NOT_NEED_MANUAL_SHIP", 10555, "全托管订单【{0}】无需手动标发");
    public static final ApiError SO_NOT_FULLY_MANAGED_ORDER_NOT_PRINT_SKU_BARCODE = new ApiError("SO_NOT_FULLY_MANAGED_ORDER_NOT_PRINT_SKU_BARCODE", 10556, "订单【{0}】不是全托管订单，禁止打印SKU条码");
    public static final ApiError SO_FULLY_MANAGED_AND_B2C_NOT_PRINT_TOGETHER = new ApiError("SO_FULLY_MANAGED_AND_B2C_NOT_PRINT_TOGETHER", 10557, "托管订单与B2C订单不支持同时打印");
    public static final ApiError SO_B2C_DELIVERY_STATUS_NOT_ALLOW_MANUAL_SHIP_FLAG = new ApiError("SO_B2C_DELIVERY_STATUS_NOT_ALLOW_MANUAL_SHIP_FLAG", 10558, "发货单【{0}】处于手动标发或已发货状态，不允许再次手动标发");
    public static final ApiError SO_MANUAL_SHIP_ALLOWED_APPROVED_PENDING_ONLY = new ApiError("SO_MANUAL_SHIP_ALLOWED_APPROVED_PENDING_ONLY", 10559, "仅审核通过且待发货的订单允许执行手动标发操作");
    public static final ApiError SO_DISTRIBUTION_MANUAL_SHIP_ALLOWED = new ApiError("SO_DISTRIBUTION_MANUAL_SHIP_ALLOWED", 10560, "仅配货中且已生成渠道与物流号的订单允许手动标发");
    public static final ApiError SO_STATUS_NOT_WAVE_CANNOT_PRINT_PICKING = new ApiError("SO_STATUS_NOT_WAVE_CANNOT_PRINT_PICKING", 10561, "单据【{0}】未生成波次，不允许操作");
    public static final ApiError SO_CONTAIN_NON_FULLY_MANAGED_ORDER_NOT_PRINT_BARCODE = new ApiError("SO_CONTAIN_NON_FULLY_MANAGED_ORDER_NOT_PRINT_BARCODE", 10562, "存在非全托管订单不能打印sku条码");
    public static final ApiError SO_DELIVERY_NOTICE_WAREHOUSE_REQUIRED = new ApiError("SO_DELIVERY_NOTICE_WAREHOUSE_REQUIRED", 10563, "发货通知单【{0}】配置的发货仓库不能为空");
    public static final ApiError SO_B2C_SHIPMENT_WAREHOUSE_REQUIRED = new ApiError("SO_B2C_SHIPMENT_WAREHOUSE_REQUIRED", 10564, "B2C发货单【{0}】配置的发货仓库不能为空");
    public static final ApiError SO_OUTSTOCK_BILL_COST_NOT_DISAPPROVE = new ApiError("SO_OUTSTOCK_BILL_COST_NOT_DISAPPROVE", 10565, "销售出库单【{0}】已生成物流费用单且已确认/暂估确认，不允许反审核");
    public static final ApiError SO_OUTBOUND_RECORD_NOT_FOUND = new ApiError("SO_OUTBOUND_RECORD_NOT_FOUND", 10566, "销售订单出库记录不存在");
    public static final ApiError SO_DELIVERY_NOTICE_RECORD_NOT_FOUND = new ApiError("SO_DELIVERY_NOTICE_RECORD_NOT_FOUND", 10567, "装箱记录不存在");
    public static final ApiError SO_B2C_DELIVERY_FINISH_PRINT_ONLY = new ApiError("SO_B2C_DELIVERY_FINISH_PRINT_ONLY", 10568, "发货单【{0}】仅在生成波次或拣货中状态下支持设置为打印完成");
    public static final ApiError SO_B2C_DELIVERY_NOT_FINISH_PRINT_ONLY = new ApiError("SO_B2C_DELIVERY_NOT_FINISH_PRINT_ONLY", 10569, "发货单【{0}】仅在生成波次或拣货中状态下支持取消完成打印");
    public static final ApiError SO_B2B_ORDER_PACK_ONLY = new ApiError("SO_B2B_ORDER_PACK_ONLY", 10570, "仅B2B订单类型的销售单允许执行装箱操作");
    public static final ApiError SO_UNPICKED_QUANTITY_SHORTAGE = new ApiError("SO_UNPICKED_QUANTITY_SHORTAGE", 10571, "未拣货数量不足，无法生成拣货单，请重新选择");
    public static final ApiError SO_DELIVERY_NOTICE_DETAIL_NOT_EXIST = new ApiError("SO_DELIVERY_NOTICE_DETAIL_NOT_EXIST", 10572, "销售通知单明细未找到");
    public static final ApiError SO_DETAIL_NOT_EXIST = new ApiError("SO_DETAIL_NOT_EXIST", 10573, "销售订单明细未找到");
    public static final ApiError SO_OUTBOUND_B2B_REQUIRED = new ApiError("SO_OUTBOUND_B2B_REQUIRED", 10574, "仅未作废的B2B订单类型销售出库单允许执行该操作");
    public static final ApiError SO_DELIVERY_REQUIRED_PENDING_NOT_APPROVED = new ApiError("SO_DELIVERY_REQUIRED_PENDING_NOT_APPROVED", 10575, "仅未作废且未审核通过的发货单允许执行该操作");
    public static final ApiError SO_TRANSFER_NOT_RETRY_OUTSTOCK = new ApiError("SO_TRANSFER_NOT_RETRY_OUTSTOCK", 10576, "发货单已通过调拨完成出库，不允许重新出库");
    public static final ApiError SO_OUTSTOCK_UPDATE_ALLOWED_WAIT_NOTIFY_ONLY = new ApiError("SO_OUTSTOCK_UPDATE_ALLOWED_WAIT_NOTIFY_ONLY", 10577, "仅待通知出库状态的单据允许修改为待通知出库");
    public static final ApiError SO_OUTSTOCK_PUSH_ALLOWED_FLAG_ONLY = new ApiError("SO_OUTSTOCK_PUSH_ALLOWED_FLAG_ONLY", 10578, "  - 仅标记为允许出库的通知单才允许下推销售出库单");
    public static final ApiError SO_NOT_FOUND = new ApiError("SO_NOT_FOUND", 10579, "销售订单不存在");
    public static final ApiError SO_OUTBOUND_PUSH_REVERSE_FORBIDDEN = new ApiError("SO_OUTBOUND_PUSH_REVERSE_FORBIDDEN", 10580, "销售订单已下推销售出库单，不支持反审核");
    public static final ApiError SO_DELIVERY_RETURN_QTY_EXCEEDS_OUTBOUND = new ApiError("SO_DELIVERY_RETURN_QTY_EXCEEDS_OUTBOUND", 10581, "退货数量不能大于已出库数量");
    public static final ApiError SO_DELIVERY_QTY_EXCEEDS_SALES = new ApiError("SO_DELIVERY_QTY_EXCEEDS_SALES", 10582, "sku【{0}】发货总数量不能大于销售数量");
    public static final ApiError SO_DELIVERY_QTY_EXCEEDS_FROZEN = new ApiError("SO_DELIVERY_QTY_EXCEEDS_FROZEN", 10582, "sku【{0}】发货数量不能大于锁定数量");
    public static final ApiError SO_DELIVERY_RETURN_NOTICE_PUSH_REVERSE_FORBIDDEN = new ApiError("SO_DELIVERY_RETURN_NOTICE_PUSH_REVERSE_FORBIDDEN", 10583, "销售退货通知单已下推，不支持反审核");
    public static final ApiError SO_DELIVERY_RETURN_SIGN_PUSH_REVERSE_FORBIDDEN = new ApiError("SO_DELIVERY_RETURN_SIGN_PUSH_REVERSE_FORBIDDEN", 10584, "销售退货签收单已下推，不支持反审核");
    public static final ApiError SO_DELIVERY_RETURN_ORDER_APPROVED_REQUIRED_NOTICE = new ApiError("SO_DELIVERY_RETURN_ORDER_APPROVED_REQUIRED_NOTICE", 10585, "仅已审核的销售退货订单才允许下推销售退货通知单");
    public static final ApiError SO_DETAIL_NOT_FOUND = new ApiError("SO_DETAIL_NOT_FOUND", 10586, "销售订单明细不存在");
    public static final ApiError SO_DELETE_FORBIDDEN = new ApiError("SO_DELETE_FORBIDDEN", 10587, "仅待提交或暂存状态的销售订单允许删除");
    public static final ApiError SO_DELIVERY_RETURN_SIGN_QTY_EXCEEDS = new ApiError("SO_DELIVERY_RETURN_SIGN_QTY_EXCEEDS", 10588, "sku【{0}】签收数量不能大于退货数量");
    public static final ApiError SO_DELIVERY_RETURN_NOTICE_APPROVED_REQUIRED_PUSH = new ApiError("SO_DELIVERY_RETURN_NOTICE_APPROVED_REQUIRED_PUSH", 10589, "仅已审核的退货通知单才允许下推销售退货单");
    public static final ApiError SO_EXPORT_CONTRACT_INVOICE_ALLOWED = new ApiError("SO_EXPORT_CONTRACT_INVOICE_ALLOWED", 10590, "未作废的待提交、审核中或已审核状态支持导出合同或发票");
    public static final ApiError SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND = new ApiError("SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND", 10591, "SKU【{0}】在销售退货单中不存在");
    public static final ApiError SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS = new ApiError("SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS", 10592, "SKU【{0}】退货数量不能大于退货单数量");
    public static final ApiError SO_NOT_APPROVED_CANNOT_PUSH_STOCKREQ = new ApiError("SO_NOT_APPROVED_CANNOT_PUSH_STOCKREQ", 10593, "销售订单【{0}】未审核完成，不支持下推备货申请单");
    public static final ApiError SO_DELIVERY_RETURN_ORDER_INBOUND_QTY_EXCEEDS = new ApiError("SO_DELIVERY_RETURN_ORDER_INBOUND_QTY_EXCEEDS", 10594, "SKU【{0}】入库数量不能大于签收数量");
    public static final ApiError SO_DELIVERY_QTY_GT_REQUIRED_QTY = new ApiError("SO_DELIVERY_QTY_GT_REQUIRED_QTY", 10595, "实际发货数量不能大于应发数量");
    public static final ApiError SO_DELIVERY_QTY_GT_AVAILABLE_QTY = new ApiError("SO_DELIVERY_QTY_GT_AVAILABLE_QTY", 10596, "应发数量不能大于可出库数量");
    public static final ApiError SO_DELIVERY_OUTBOUND_DETAIL_REQUIRED = new ApiError("SO_DELIVERY_OUTBOUND_DETAIL_REQUIRED", 10597, "销售出库单明细不能为空");
    public static final ApiError SO_DELIVERY_QTY_GT_STOCK = new ApiError("SO_DELIVERY_QTY_GT_STOCK", 10598, "应发数量不能大于库存数量");
    public static final ApiError SO_DELIVERY_RETURN_SIGN_APPROVED_REQUIRED_PUSH_INBOUND = new ApiError("SO_DELIVERY_RETURN_SIGN_APPROVED_REQUIRED_PUSH_INBOUND", 10599, "仅已审核的销售退货签收单才允许下推退货入库单");
    public static final ApiError SO_CHANGE_APPROVED_REQUIRED = new ApiError("SO_CHANGE_APPROVED_REQUIRED", 10600, "仅已审核的销售订单允许发起变更");
    public static final ApiError SO_CHANGE_NOT_FOUND = new ApiError("SO_CHANGE_NOT_FOUND", 10601, "销售订单变更单不存在");
    public static final ApiError SO_CHANGE_DETAIL_NOT_FOUND = new ApiError("SO_CHANGE_DETAIL_NOT_FOUND", 10602, "销售订单变更明细不存在");
    public static final ApiError SO_ASSOCIATED_DOC_DELETE_FORBIDDEN = new ApiError("SO_ASSOCIATED_DOC_DELETE_FORBIDDEN", 10603, "单据【{0}】已生成下游单据，无法删除");
    public static final ApiError SO_CHANGE_CONFLICT = new ApiError("SO_CHANGE_CONFLICT", 10604, "存在多个销售订单变更记录，请重新选择");
    public static final ApiError SO_ASSOCIATED_DOC_REVERSE_FORBIDDEN = new ApiError("SO_ASSOCIATED_DOC_REVERSE_FORBIDDEN", 10605, "销售订单存在关联单据，不支持反审核");
    public static final ApiError SO_CHANGE_IN_PROGRESS = new ApiError("SO_CHANGE_IN_PROGRESS", 10606, "存在销售订单正在变更中，无法下推单据");
    public static final ApiError SO_APPROVED_REQUIRED_PUSH = new ApiError("SO_APPROVED_REQUIRED_PUSH", 10607, "仅审核通过的销售订单允许下推单据");
    public static final ApiError SO_NOT_VOID_REQUIRED_PUSH = new ApiError("SO_NOT_VOID_REQUIRED_PUSH", 10608, "仅未作废的销售订单允许下推单据");
    public static final ApiError SO_DELIVERY_RETURN_SIGN_TOTAL_QTY_EXCEEDS = new ApiError("SO_DELIVERY_RETURN_SIGN_TOTAL_QTY_EXCEEDS", 10609, "SKU【{0}】实退总数量不能大于签收数量");
    public static final ApiError SO_CHANGE_IN_REVERSE_FORBIDDEN = new ApiError("SO_CHANGE_IN_REVERSE_FORBIDDEN", 10610, "销售订单处于变更中，不支持反审核");
    public static final ApiError SO_CHANGE_QTY_LT_DELIVERY_NOTICE = new ApiError("SO_CHANGE_QTY_LT_DELIVERY_NOTICE", 10611, "销售订单变更数量不能小于已下推的发货通知数量");
    public static final ApiError SO_B2C_DELIVERY_K3_CLOUD_OUTBOUND_WAREHOUSE_NOT_FOUND = new ApiError("SO_B2C_DELIVERY_K3_CLOUD_OUTBOUND_WAREHOUSE_NOT_FOUND", 10612, "同步金蝶B2C销售出库单时未找到对应仓库【{0}】");
    public static final ApiError SO_B2C_DELIVERY_K3_CLOUD_RETURN_WAREHOUSE_NOT_FOUND = new ApiError("SO_B2C_DELIVERY_K3_CLOUD_RETURN_WAREHOUSE_NOT_FOUND", 10613, "同步金蝶B2C销售退货单未找到对应的仓库【{0}】");
    public static final ApiError SO_B2C_DELIVERY_K3_CLOUD_RETURN_SKU_NOT_FOUND = new ApiError("SO_B2C_DELIVERY_K3_CLOUD_RETURN_SKU_NOT_FOUND", 10614, "同步金蝶B2C销售退货单时未找到对应SKU【{0}】");
    public static final ApiError SO_DEMAND_DATE_GT_ORDER_DATE = new ApiError("SO_DEMAND_DATE_GT_ORDER_DATE", 10615, "要货日期必须晚于单据日期");
    public static final ApiError SO_B2C_NOT_FOUND = new ApiError("SO_B2C_NOT_FOUND", 10616, "B2C销售订单不存在");
    public static final ApiError SO_B2C_WILDBERRIES_NOT_ALLOWED = new ApiError("SO_B2C_WILDBERRIES_NOT_ALLOWED", 10617, "WB平台订单【{0}】不支持拆分或合并");
    public static final ApiError SO_B2C_PLATFORM_SHOP_REQUIRED = new ApiError("SO_B2C_PLATFORM_SHOP_REQUIRED", 10618, "B2C销售订单【{0}】平台和店铺不能为空");
    public static final ApiError SO_B2C_LOGISTICS_NOT_FOUND = new ApiError("SO_B2C_LOGISTICS_NOT_FOUND", 10619, "B2C销售订单物流信息不存在");
    public static final ApiError SO_B2C_RECEIVER_NOT_FOUND = new ApiError("SO_B2C_RECEIVER_NOT_FOUND", 10620, "B2C销售订单买家信息不存在");
    public static final ApiError SO_B2C_EXTEND_NOT_FOUND = new ApiError("SO_B2C_EXTEND_NOT_FOUND", 10621, "B2C销售订单扩展信息不存在");
    public static final ApiError SO_B2C_DETAIL_NOT_FOUND = new ApiError("SO_B2C_DETAIL_NOT_FOUND", 10622, "B2C销售订单明细不存在");
    public static final ApiError SO_B2C_DETAIL_IMPORT_FAILED = new ApiError("SO_B2C_DETAIL_IMPORT_FAILED", 10758, "B2C销售订单明细导入失败");
    public static final ApiError SO_B2C_DETAIL_SAVE_OR_UPDATE_FAILED = new ApiError("SO_B2C_DETAIL_SAVE_OR_UPDATE_FAILED", 10759, "B2C销售订单明细批量保存或更新失败");
    public static final ApiError SO_B2C_DELIVERY_WAREHOUSE_CONFLICT = new ApiError("SO_B2C_DELIVERY_WAREHOUSE_CONFLICT", 10623, "B2C销售订单【{0}】存在多个发货仓库，不支持提交发货");
    public static final ApiError SO_B2C_SKU_INVENTORY_NOT_FOUND = new ApiError("SO_B2C_SKU_INVENTORY_NOT_FOUND", 10624, "B2C销售订单【{0}】SKU【{1}】在仓库【{2}】中未找到可用库存");
    public static final ApiError SO_B2C_MERGE_PLATFORM_CONFLICT = new ApiError("SO_B2C_MERGE_PLATFORM_CONFLICT", 10625, "合并订单要求销售平台一致");
    public static final ApiError SO_B2C_MERGE_SHOP_CONFLICT = new ApiError("SO_B2C_MERGE_SHOP_CONFLICT", 10626, "合并订单要求店铺一致");
    public static final ApiError SO_B2C_MERGE_CURRENCY_CONFLICT = new ApiError("SO_B2C_MERGE_CURRENCY_CONFLICT", 10627, "合并订单要求币种一致");
    public static final ApiError SO_B2C_MERGE_LOGISTICS_METHOD_CONFLICT = new ApiError("SO_B2C_MERGE_LOGISTICS_METHOD_CONFLICT", 10628, "合并订单要求物流方式一致");
    public static final ApiError SO_B2C_MERGE_BUYER_CONFLICT = new ApiError("SO_B2C_MERGE_BUYER_CONFLICT", 10629, "合并订单要求买家一致");
    public static final ApiError SO_B2C_MERGE_RECEIVER_CONFLICT = new ApiError("SO_B2C_MERGE_RECEIVER_CONFLICT", 10630, "合并订单要求收货人一致");
    public static final ApiError SO_B2C_MERGE_ADDRESS_CONFLICT = new ApiError("SO_B2C_MERGE_ADDRESS_CONFLICT", 10631, "合并订单要求收货地址一致");
    public static final ApiError SO_B2C_MERGE_WAREHOUSE_CONFLICT = new ApiError("SO_B2C_MERGE_WAREHOUSE_CONFLICT", 10632, "合并订单要求发货仓库一致");
    public static final ApiError SO_B2C_CANCEL_MERGE_NOT_SUPPORTED = new ApiError("SO_B2C_CANCEL_MERGE_NOT_SUPPORTED", 10633, "订单【{0}】非合并订单，不支持取消合并");
    public static final ApiError SO_B2C_MERGE_NOT_SPLIT = new ApiError("SO_B2C_MERGE_NOT_SPLIT", 10634, "订单【{0}】为合并订单，不支持拆分");
    public static final ApiError SO_B2C_SPLIT_QTY_EXCEEDS = new ApiError("SO_B2C_SPLIT_QTY_EXCEEDS", 10635, "订单【{0}】SKU【{1}】拆分数量【{2}】不能大于原数量【{3}】");
    public static final ApiError SO_B2C_NOT_SPLIT_ORDER = new ApiError("SO_B2C_NOT_SPLIT_ORDER", 10636, "B2C销售订单【{0}】非拆分后订单，不支持取消拆分");
    public static final ApiError SO_B2C_SPLIT_ORDER_REQUIRED = new ApiError("SO_B2C_SPLIT_ORDER_REQUIRED", 10637, "请先选择需要拆分的订单");
    public static final ApiError SO_B2C_CATEGORY_NOT_FOUND = new ApiError("SO_B2C_CATEGORY_NOT_FOUND", 10638, "B2C销售订单分类信息不存在");
    public static final ApiError SO_B2C_CANCEL_NOT_SUPPORTED = new ApiError("SO_B2C_CANCEL_NOT_SUPPORTED", 10639, "单据【{0}】不支持取消【{1}】操作");
    public static final ApiError SO_B2C_REVERSE_VOID_FORBIDDEN = new ApiError("SO_B2C_REVERSE_VOID_FORBIDDEN", 10640, "单据【{0}】未作废，不支持反作废");
    public static final ApiError SO_B2C_MERGE_SIZE_REQUIRED = new ApiError("SO_B2C_MERGE_SIZE_REQUIRED", 10641, "请至少选择两条订单进行合并");
    public static final ApiError SO_B2C_CHILD_NOT_FOUND = new ApiError("SO_B2C_CHILD_NOT_FOUND", 10642, "B2C销售订单【{0}】未找到拆分后的子订单");
    public static final ApiError SO_B2C_LOGISTICS_CODE_ONLY_DISTRIBUTION = new ApiError("SO_B2C_LOGISTICS_CODE_ONLY_DISTRIBUTION", 10643, "B2C销售订单【{0}】仅在配货中状态支持获取物流单号");
    public static final ApiError SO_B2C_SUBMIT_DELIVERY_NOT_ALLOWED = new ApiError("SO_B2C_SUBMIT_DELIVERY_NOT_ALLOWED", 10644, "B2C销售订单【{0}】仅在配货中状态支持提交发货");
    public static final ApiError SO_B2C_DISTRIBUTION_STATUS_REQUIRED = new ApiError("SO_B2C_DISTRIBUTION_STATUS_REQUIRED", 10645, "B2C销售订单【{0}】仅支持待配货或配货中状态");
    public static final ApiError SO_B2C_CANCEL_MERGE_STATUS_LIMIT = new ApiError("SO_B2C_CANCEL_MERGE_STATUS_LIMIT", 10646, "B2C销售订单【{0}】仅待提交或审核不通过状态支持取消合并");
    public static final ApiError SO_B2C_LOGISTICS_METHOD_NOT_FOUND = new ApiError("SO_B2C_LOGISTICS_METHOD_NOT_FOUND", 10647, "B2C销售订单物流方式不存在");
    public static final ApiError SO_B2C_CUSTOMER_NOT_FOUND = new ApiError("SO_B2C_CUSTOMER_NOT_FOUND", 10648, "B2C销售客户不存在");
    public static final ApiError SO_B2C_FINANCE_NOT_FOUND = new ApiError("SO_B2C_FINANCE_NOT_FOUND", 10649, "B2C销售订单财务信息不存在");
    public static final ApiError SO_B2C_NOT_NEED_MERGE = new ApiError("SO_B2C_NOT_NEED_MERGE", 10650, "销售订单【{0}】无需合并");
    public static final ApiError SO_NOT_APPROVED_PUSH_FORBIDDEN = new ApiError("SO_NOT_APPROVED_PUSH_FORBIDDEN", 10651, "销售订单【{0}】未审核完成，不支持下推");
    public static final ApiError SO_B2C_PLATFORM_ORDER_VOIDED = new ApiError("SO_B2C_PLATFORM_ORDER_VOIDED", 10652, "全托管订单【{0}】平台状态为已作废，不支持提交发货");
    public static final ApiError SO_B2C_ORDER_VOIDED = new ApiError("SO_B2C_ORDER_VOIDED", 10653, "订单【{0}】已作废，不支持提交发货");
    public static final ApiError SO_B2C_LOGISTICS_CHANNEL_REQUIRED = new ApiError("SO_B2C_LOGISTICS_CHANNEL_REQUIRED", 10654, "B2C销售订单【{0}】物流渠道不能为空");
    public static final ApiError SO_B2C_LOGISTICS_CHANNEL_AND_NO_REQUIRED = new ApiError("SO_B2C_LOGISTICS_CHANNEL_AND_NO_REQUIRED", 10655, "B2C销售订单【{0}】物流渠道和物流单号不能为空");
    public static final ApiError SO_B2C_UPDATE_CATEGORY_FORBIDDEN = new ApiError("SO_B2C_UPDATE_CATEGORY_FORBIDDEN", 10656, "冻结中或已作废的订单不支持更新分类");
    public static final ApiError SO_B2C_UPDATE_REMARK_FORBIDDEN = new ApiError("SO_B2C_UPDATE_REMARK_FORBIDDEN", 10657, "已作废的订单不支持更新备注");
    public static final ApiError SO_B2C_NOT_APPROVED_DISTRIBUTION_FORBIDDEN = new ApiError("SO_B2C_NOT_APPROVED_DISTRIBUTION_FORBIDDEN", 10658, "B2C销售订单【{0}】未审核，不支持配货");
    public static final ApiError SO_B2C_SUBMIT_FORBIDDEN_WHEN_FROZEN_OR_VOIDED = new ApiError("SO_B2C_SUBMIT_FORBIDDEN_WHEN_FROZEN_OR_VOIDED", 10659, "B2C销售订单【{0}】冻结中或已作废，不支持提交");
    public static final ApiError SO_B2C_SPLIT_FORBIDDEN_BY_STATUS = new ApiError("SO_B2C_SPLIT_FORBIDDEN_BY_STATUS", 10660, "冻结中、已作废或待发货状态不支持拆分");
    public static final ApiError SO_B2C_MERGE_FORBIDDEN_BY_STATUS = new ApiError("SO_B2C_MERGE_FORBIDDEN_BY_STATUS", 10661, "冻结中、已作废、待发货或已发货状态不支持合并");
    public static final ApiError SO_B2C_SHOPEE_SPLIT_FORBIDDEN = new ApiError("SO_B2C_SHOPEE_SPLIT_FORBIDDEN", 10662, "B2C销售订单【{0}】为Shopee订单，不支持拆分");
    public static final ApiError SO_B2C_MERCADO_SPLIT_FORBIDDEN = new ApiError("SO_B2C_MERCADO_SPLIT_FORBIDDEN", 10663, "B2C销售订单【{0}】为Mercado订单，不支持拆分");
    public static final ApiError SO_B2C_MERGE_FBA_FORBIDDEN = new ApiError("SO_B2C_MERGE_FBA_FORBIDDEN", 10664, "B2C销售订单【{0}】为FBA订单，不支持合并");
    public static final ApiError SO_B2C_MERGE_CAINIAO_FORBIDDEN = new ApiError("SO_B2C_MERGE_CAINIAO_FORBIDDEN", 10665, "B2C销售订单【{0}】为菜鸟官方仓订单，不支持合并");
    public static final ApiError SO_B2C_MERGE_TAX_ORDER_FORBIDDEN = new ApiError("SO_B2C_MERGE_TAX_ORDER_FORBIDDEN", 10666, "B2C销售订单【{0}】为速卖通已税订单，不支持合并");
    public static final ApiError SO_B2C_SHOPEE_MERGE_FORBIDDEN = new ApiError("SO_B2C_SHOPEE_MERGE_FORBIDDEN", 10667, "B2C销售订单【{0}】为shopee订单不支持合并");
    public static final ApiError SO_B2C_MERCADO_MERGE_FORBIDDEN = new ApiError("SO_B2C_MERCADO_MERGE_FORBIDDEN", 10668, "B2C销售订单【{0}】为mercado订单不支持合并");
    public static final ApiError SO_B2C_TIKTOK_MERGE_FORBIDDEN = new ApiError("SO_B2C_TIKTOK_MERGE_FORBIDDEN", 10669, "B2C销售订单【{0}】为TikTok订单不支持合并");
    public static final ApiError SO_B2C_PAYMENT_REQUIRED = new ApiError("SO_B2C_PAYMENT_REQUIRED", 10670, "B2C销售订单【{0}】未完成付款，不支持任何操作");
    public static final ApiError SO_DELIVERY_WAREHOUSE_NOT_FOUND = new ApiError("SO_DELIVERY_WAREHOUSE_NOT_FOUND", 10671, "销售订单发货仓库不存在，不支持提交发货");
    public static final ApiError SO_B2C_ORDER_FETCH_FAILED = new ApiError("SO_B2C_ORDER_FETCH_FAILED", 10672, "订单拉取失败，请刷新订单后重试");
    public static final ApiError SO_B2C_WAREHOUSE_NOT_FOUND = new ApiError("SO_B2C_WAREHOUSE_NOT_FOUND", 10673, "B2C销售订单发货仓库不存在");
    public static final ApiError SO_B2C_ALREADY_MERGED_OR_SPLIT = new ApiError("SO_B2C_ALREADY_MERGED_OR_SPLIT", 10674, "B2C销售订单【{0}】已合并或已拆分，不支持再次合并");
    public static final ApiError SO_DELIVERY_STATUS_REQUIRED_FOR_INTERCEPT = new ApiError("SO_DELIVERY_STATUS_REQUIRED_FOR_INTERCEPT", 10675, "仅待发货状态的订单支持发起拦截");
    public static final ApiError SO_DELIVERY_ALREADY_INTERCEPTED = new ApiError("SO_DELIVERY_ALREADY_INTERCEPTED", 10676, "订单已取消或已被拦截");
    public static final ApiError SO_CHANGE_ALREADY_TERMINATED = new ApiError("SO_CHANGE_ALREADY_TERMINATED", 10677, "销售订单已存在终止记录，不支持再次终止");
    public static final ApiError SO_PUSH_MACHINE_DATA_NOT_FOUND = new ApiError("SO_PUSH_MACHINE_DATA_NOT_FOUND", 10678, "未找到可下推加工单的销售订单数据");
    public static final ApiError SO_B2C_REVERSE_APPROVE_STATUS_LIMIT = new ApiError("SO_B2C_REVERSE_APPROVE_STATUS_LIMIT", 10679, "仅待配货或配货中状态的订单支持反审核");
    public static final ApiError SO_B2C_APPROVED_REQUIRED_FOR_DELIVERY = new ApiError("SO_B2C_APPROVED_REQUIRED_FOR_DELIVERY", 10680, "仅审核通过的订单支持提交发货");
    public static final ApiError SO_OUTBOUND_EXISTS_MAPPING_UPDATE_FORBIDDEN = new ApiError("SO_OUTBOUND_EXISTS_MAPPING_UPDATE_FORBIDDEN", 10681, "已生成销售出库单，不支持修改SKU映射关系");
    public static final ApiError SO_DELIVERY_EXISTS_MAPPING_UPDATE_FORBIDDEN = new ApiError("SO_DELIVERY_EXISTS_MAPPING_UPDATE_FORBIDDEN", 10682, "已生成发货单，不支持修改SKU映射关系");
    public static final ApiError SO_B2C_SOURCE_ONLY_MAPPING_UPDATE_ALLOWED = new ApiError("SO_B2C_SOURCE_ONLY_MAPPING_UPDATE_ALLOWED", 10683, "非平台来源的B2C销售订单不支持修改SKU映射关系");
    public static final ApiError SO_B2C_DISTRIBUTION_DECLARE_STATUS_INVALID = new ApiError("SO_B2C_DISTRIBUTION_DECLARE_STATUS_INVALID", 10684, "B2C销售订单【{0}】仅支持已审核且处于配货中的订单操作");
    public static final ApiError SO_B2C_DECLARE_ALREADY_EXISTS = new ApiError("SO_B2C_DECLARE_ALREADY_EXISTS", 10685, "B2C销售订单【{0}】已存在申报信息，不再执行规则匹配");
    public static final ApiError SO_B2C_DECLARE_INFO_NOT_FOUND = new ApiError("SO_B2C_DECLARE_INFO_NOT_FOUND", 10686, "销售订单【{0}】申报信息不存在");
    public static final ApiError SO_B2C_TIKTOK_SPLIT_FORBIDDEN_WITH_REASON = new ApiError("SO_B2C_TIKTOK_SPLIT_FORBIDDEN_WITH_REASON", 10687, "B2C销售订单【{0}】在TikTok平台不允许拆分，平台提示【{1}】");
    public static final ApiError SO_B2C_TIKTOK_SPLIT_SKU_FORBIDDEN = new ApiError("SO_B2C_TIKTOK_SPLIT_SKU_FORBIDDEN", 10688, "订单【{0}】SKU【{1}】在TikTok平台中不允许把一个sku拆分成多个单据分开发货");
    public static final ApiError SO_B2C_TIKTOK_SPLIT_FAILED = new ApiError("SO_B2C_TIKTOK_SPLIT_FAILED", 10689, "订单【{0}】TikTok拆分订单失败");
    public static final ApiError SO_B2C_SPLIT_BY_WAREHOUSE_FORBIDDEN = new ApiError("SO_B2C_SPLIT_BY_WAREHOUSE_FORBIDDEN", 10690, "订单明细仓库一致，无法按仓库维度拆分");
    public static final ApiError SO_B2C_LOGISTICS_PLATFORM_REQUIRED = new ApiError("SO_B2C_LOGISTICS_PLATFORM_REQUIRED", 10691, "B2C销售订单【{0}】物流下单平台不能为空");
    public static final ApiError SO_B2C_MULTI_CHANNEL_FORBIDDEN = new ApiError("SO_B2C_MULTI_CHANNEL_FORBIDDEN", 10692, "B2C销售订单【{0}】不支持设置多个销售渠道");
    public static final ApiError SO_B2C_SPLIT_KOL_FORBIDDEN = new ApiError("SO_B2C_SPLIT_KOL_FORBIDDEN", 10727, "销售订单由寄样申请单生成，无法拆单");
    public static final ApiError SO_B2C_MAGALU_SPLIT_FORBIDDEN = new ApiError("SO_B2C_MAGALU_SPLIT_FORBIDDEN", 10728, "magalu平台订单不支持拆单");
    public static final ApiError SO_B2C_MAGALU_MERGE_FORBIDDEN = new ApiError("SO_B2C_MAGALU_MERGE_FORBIDDEN", 10729, "magalu平台订单不支持合并");
    public static final ApiError SO_DETAIL_SKU_ALL_EMPTY_FORBIDDEN = new ApiError("SO_DETAIL_SKU_ALL_EMPTY_FORBIDDEN", 10693, "销售订单【{0}】明细中sku不能全部为空");
    public static final ApiError SO_REPLACE_SKU_STATUS_INVALID = new ApiError("SO_REPLACE_SKU_STATUS_INVALID", 10694, "销售订单【{0}】只能在待提交、审核不通过或已发货状态更换发货SKU");
    public static final ApiError SO_B2B_SALESMAN_CHANGE = new ApiError("SO_B2B_SALESMAN_CHANGE", 10695, "b2b客户销售员变更单");
    public static final ApiError SO_REFUND_ORDER_DETAIL = new ApiError("SO_REFUND_ORDER_DETAIL", 10696, "退款订单明细");
    public static final ApiError SO_PLATFORM_ORDER_MERGE_TOO_LONG = new ApiError("SO_PLATFORM_ORDER_MERGE_TOO_LONG", 10697, "合并后的平台订单号长度过长");
    public static final ApiError SO_DELIVERY_AUTO_SUBMIT_OPTION_LIMIT = new ApiError("SO_DELIVERY_AUTO_SUBMIT_OPTION_LIMIT", 10698, "仅允许选择一个物流匹配自动处理选项");
    public static final ApiError SO_OUTBOUND_ALREADY_GENERATED_TERMINATE_FORBIDDEN = new ApiError("SO_OUTBOUND_ALREADY_GENERATED_TERMINATE_FORBIDDEN", 10699, "【{0}】已生成销售出库单，不支持终止");
    public static final ApiError SO_PICKLIST_NOT_PROCESSED_FORBIDDEN = new ApiError("SO_PICKLIST_NOT_PROCESSED_FORBIDDEN", 10700, "拣货单未处理，无法生成");
    public static final ApiError SO_RETURN_NOTICE_SKU_NOT_FOUND = new ApiError("SO_RETURN_NOTICE_SKU_NOT_FOUND", 10701, "sku【{0}】在退货通知单中不存在");
    public static final ApiError SO_RETURN_SIGN_SKU_NOT_FOUND = new ApiError("SO_RETURN_SIGN_SKU_NOT_FOUND", 10702, "sku【{0}】在退货签收单中不存在");
    public static final ApiError SO_RETURN_QTY_EXCEEDS_EXPECTED = new ApiError("SO_RETURN_QTY_EXCEEDS_EXPECTED", 10703, "sku【{0}】实退总数量不能大于应退数量");
    public static final ApiError SO_RETURN_NOTICE_DETAIL_REQUIRED = new ApiError("SO_RETURN_NOTICE_DETAIL_REQUIRED", 10704, "退货通知单明细不能为空");
    public static final ApiError SO_RETURN_SIGN_DETAIL_REQUIRED = new ApiError("SO_RETURN_SIGN_DETAIL_REQUIRED", 10705, "退货签收单明细不能为空");
    public static final ApiError SO_RETURN_INBOUND_DETAIL_REQUIRED = new ApiError("SO_RETURN_INBOUND_DETAIL_REQUIRED", 10706, "退货入库单明细不能为空");
    public static final ApiError SO_PRICE_EXPIRE_BEFORE_EFFECTIVE = new ApiError("SO_PRICE_EXPIRE_BEFORE_EFFECTIVE", 10707, "销售价目表中SKU【{0}】的失效时间不能早于生效时间");
    public static final ApiError SO_PRICE_DATE_RANGE_OVERLAP = new ApiError("SO_PRICE_DATE_RANGE_OVERLAP", 10708, "销售价目表中SKU【{0}】的价格时间区间存在重叠");
    public static final ApiError SO_PRICE_NOT_FOUND = new ApiError("SO_PRICE_NOT_FOUND", 10709, "未找到销售价目表数据");
    public static final ApiError SO_PRICE_DETAIL_NOT_FOUND = new ApiError("SO_PRICE_DETAIL_NOT_FOUND", 10710, "未找到销售价目表明细");
    public static final ApiError SO_ORG_NOT_REPEAT = new ApiError("SO_ORG_NOT_REPEAT", 10711, "只有相同的销售组织可以批量变更报价");
    public static final ApiError SO_PRICE_INTERVAL_INVALID = new ApiError("SO_PRICE_INTERVAL_INVALID", 10712, "SKU【{0}】价格区间起始值不能大于或等于结束值");
    public static final ApiError SO_ALREADY_REF_DOWNSTREAM_BILL_FORBIDDEN = new ApiError("SO_ALREADY_REF_DOWNSTREAM_BILL_FORBIDDEN", 10713, "销售订单已存在关联单据【{0}】，不支持该操作");
    public static final ApiError SO_RETURN_DETAIL_SKU_NOT_FOUND = new ApiError("SO_RETURN_DETAIL_SKU_NOT_FOUND", 10714, "SKU在销售退货单中未找到");
    public static final ApiError SO_B2C_ADD_GIFT_STATUS_FORBIDDEN = new ApiError("SO_B2C_ADD_GIFT_STATUS_FORBIDDEN", 10715, "非待提交或审核不通过状态的订单不允许添加赠品");
    public static final ApiError SO_WDT_SALES_RAW_TRADE_PUSHSELF = new ApiError("SO_WDT_SALES_RAW_TRADE_PUSHSELF", 10716, "ERP原始订单推送旺店通结果：新增订单的数量:【{0}】，更新订单的数量:【{1}】，错误信息:【{2}】");
    public static final ApiError SO_LOGISTICS_WAYBILL_NOT_OBTAINED = new ApiError("SO_LOGISTICS_WAYBILL_NOT_OBTAINED", 92118, "【{0}】面单未获取，无法打印，请获取后操作！");
    public static final ApiError SO_THIRD_DELIVERY_INTERCEPT_ONLY_WAIT_SHIPPED = new ApiError("SO_THIRD_DELIVERY_INTERCEPT_ONLY_WAIT_SHIPPED", 92248, "只有待发货、异常订单允许发货拦截");
    public static final ApiError SO_THIRD_DELIVERY_MANUAL_ONLY_B2B_DISABLED = new ApiError("SO_THIRD_DELIVERY_MANUAL_ONLY_B2B_DISABLED", 92248, "只有未开启B2B发货的允许手动发货");
    public static final ApiError SO_THIRD_DELIVERY_ONLY_WAIT_SHIPPED = new ApiError("SO_THIRD_DELIVERY_ONLY_WAIT_SHIPPED", 92248, "只有待发货状态的允许发货");
    public static final ApiError SO_THIRD_DELIVERY_GENERATE_OUTSTOCK_ONLY_SHIPPED = new ApiError("SO_THIRD_DELIVERY_GENERATE_OUTSTOCK_ONLY_SHIPPED", 92248, "只有已发货状态的允许生成销售出库单");
    public static final ApiError SO_THIRD_DELIVERY_DELETE_ONLY_FAILED_OR_CANCELED = new ApiError("SO_THIRD_DELIVERY_DELETE_ONLY_FAILED_OR_CANCELED", 92248, "只有创建失败、取消发货允许删除");
    public static final ApiError SO_B2C_GET_EXCHANGE_RATE_FAILED = new ApiError("SO_B2C_GET_EXCHANGE_RATE_FAILED", 10718, "获取汇率异常-汇率获取失败，请重新获取");
    public static final ApiError SO_RETURN_EXCHANGE_RATE_REQUIRED = new ApiError("SO_RETURN_EXCHANGE_RATE_REQUIRED", 10719, "销售订单明细【{0}】汇率为空，无法计算本位币金额");
    public static final ApiError SO_RETURN_RECEIVE_QTY_INVALID = new ApiError("SO_RETURN_RECEIVE_QTY_INVALID", 10720, "sku【{0}】签收数量异常，实际值：{1}");
    public static final ApiError SO_RETURN_RECEIVE_AMOUNT_MISSING = new ApiError("SO_RETURN_RECEIVE_AMOUNT_MISSING", 10721, "sku【{0}】签收金额数据缺失");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_DUPLICATE_CODE = new ApiError("SO_RETURN_INSTOCK_IMPORT_DUPLICATE_CODE", 10756, "退货入库单号在导入表中重复了，请调整");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_BATCH_ABORT = new ApiError("SO_RETURN_INSTOCK_IMPORT_BATCH_ABORT", 10757, "本批存在其他错误，整批未处理");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_PERSIST_FAILED = new ApiError("SO_RETURN_INSTOCK_IMPORT_PERSIST_FAILED", 10758, "导入落库失败");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_CODE_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_CODE_NOT_FOUND", 10759, "无法识别退货入库单号，请确定编码是否正确或是否存在");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_UPDATE_STATUS_INVALID = new ApiError("SO_RETURN_INSTOCK_IMPORT_UPDATE_STATUS_INVALID", 10760, "仅有待提交且未作废退货入库单可修改");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_SOURCE_TYPE_FORBIDDEN = new ApiError("SO_RETURN_INSTOCK_IMPORT_SOURCE_TYPE_FORBIDDEN", 10761, "仅支持来源\"三方仓/手工建单\"的退货入库单修改");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_CUSTOMER_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_CUSTOMER_NOT_FOUND", 10762, "未能找到客户，请确定客户是否正确/已启用");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_CUSTOMER_CHANGE_FORBIDDEN = new ApiError("SO_RETURN_INSTOCK_IMPORT_CUSTOMER_CHANGE_FORBIDDEN", 10763, "仅有未映射\"退货单号\"的退货入库单的客户信息可更新");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_INVENTORY_ORG_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_INVENTORY_ORG_NOT_FOUND", 10764, "未能找到库存组织，请确定库存组织是否正确/已启用");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_CURRENCY_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_CURRENCY_NOT_FOUND", 10765, "该币种未在系统枚举值找到，请确定是否正确");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_BILL_TYPE_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_BILL_TYPE_NOT_FOUND", 10766, "该单据类型未在系统枚举值找到，请确定是否正确");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_BILL_DATE_INVALID = new ApiError("SO_RETURN_INSTOCK_IMPORT_BILL_DATE_INVALID", 10767, "入库日期非日期格式，请调整");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_RETURN_QTY_INVALID = new ApiError("SO_RETURN_INSTOCK_IMPORT_RETURN_QTY_INVALID", 10768, "退货数量不能小于1");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_ADD_CUSTOMER_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_ADD_CUSTOMER_NOT_FOUND", 10769, "未找到有效客户{0}");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_ADD_CURRENCY_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_ADD_CURRENCY_NOT_FOUND", 10770, "未找到币别{0}");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_ADD_SKU_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_ADD_SKU_NOT_FOUND", 10771, "未找到有效SKU{0}");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_NOT_FOUND", 10772, "未找到有效仓库：{0}");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_LOCATION_NOT_FOUND = new ApiError("SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_LOCATION_NOT_FOUND", 10773, "仓库:{0}未找到有效仓位：{1}");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_TASK_CREATE_FAILED = new ApiError("SO_RETURN_INSTOCK_IMPORT_TASK_CREATE_FAILED", 10774, "创建销售退货入库单导入任务失败");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_TASK_FAILED = new ApiError("SO_RETURN_INSTOCK_IMPORT_TASK_FAILED", 10775, "销售退货入库单导入失败");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_CUSTOMER_DUPLICATE = new ApiError("SO_RETURN_INSTOCK_IMPORT_CUSTOMER_DUPLICATE", 10776, "存在多个同名客户【{0}】，请人工确认");
    public static final ApiError SO_RETURN_INSTOCK_IMPORT_SKU_OCCUPY_FAILED = new ApiError("SO_RETURN_INSTOCK_IMPORT_SKU_OCCUPY_FAILED", 10777, "单据已创建但SKU占用状态更新失败");
    public static final ApiError SO_B2C_NOT_OUTBOUND_DETAIL_WAREHOUSE_UPDATE_FAILED = new ApiError("SO_B2C_NOT_OUTBOUND_DETAIL_WAREHOUSE_UPDATE_FAILED", 10753, "不出库发货失败：销售订单明细仓库未成功落库，请刷新后重试");
    public static final ApiError SO_B2C_NOT_OUTBOUND_LOGISTICS_UPDATE_FAILED = new ApiError("SO_B2C_NOT_OUTBOUND_LOGISTICS_UPDATE_FAILED", 10754, "不出库发货失败：销售订单物流信息更新失败，请刷新后重试");
    public static final ApiError SO_B2C_NOT_OUTBOUND_STATUS_UPDATE_FAILED = new ApiError("SO_B2C_NOT_OUTBOUND_STATUS_UPDATE_FAILED", 10755, "不出库发货失败：销售订单状态更新失败，请刷新后重试");
    public static final ApiError SO_CHANGE_DELETE_ALL_DETAIL_FORBIDDEN = new ApiError("SO_CHANGE_DELETE_ALL_DETAIL_FORBIDDEN", 10756, "销售变更单不允许删除所有的订单明细");
    public static final ApiError SO_OUTSTOCK_AMOUNT_MISMATCH_SUBMIT = new ApiError("SO_OUTSTOCK_AMOUNT_MISMATCH_SUBMIT", 10756, "提交失败，出库单明细金额=0，但上游销售订单明细金额非0，请核实销售出库单是否拉取有异");
    public static final ApiError SO_OUTSTOCK_AMOUNT_MISMATCH_APPROVE = new ApiError("SO_OUTSTOCK_AMOUNT_MISMATCH_APPROVE", 10757, "不可审核通过：出库单明细金额=0，但上游销售订单明细金额非0，请核实销售出库单是否拉取有异");
    public static final ApiError SO_OUTSTOCK_UPSTREAM_AMOUNT_CHECK_UNAVAILABLE = new ApiError("SO_OUTSTOCK_UPSTREAM_AMOUNT_CHECK_UNAVAILABLE", 10760, "查询上游销售订单明细异常，请稍后重试或人工核实");
    public static final ApiError SO_B2C_DELIVERY_TRANSFER_NOT_PERSISTED = new ApiError("SO_B2C_DELIVERY_TRANSFER_NOT_PERSISTED", 10750, "发货单【{0}】中转调拨单未落库，不允许生成销售出库单");
    public static final ApiError SO_B2C_DELIVERY_TRANSFER_NOT_APPROVED = new ApiError("SO_B2C_DELIVERY_TRANSFER_NOT_APPROVED", 10751, "发货单【{0}】关联的中转调拨单【{1}】未审核通过，不允许生成销售出库单");
    public static final ApiError SO_B2C_DELIVERY_MULTI_WAREHOUSE_NOT_SUPPORTED = new ApiError("SO_B2C_DELIVERY_MULTI_WAREHOUSE_NOT_SUPPORTED", 10752, "发货单【{0}】明细存在多个发货仓库，不支持校验中转调拨单");
    public static final ApiError SO_DELIVERY_NOTICE_CUSTOMER_COUNTRY_INCONSISTENT = new ApiError("SO_DELIVERY_NOTICE_CUSTOMER_COUNTRY_INCONSISTENT", 13710, "所选发货通知单客户国家不一致");
    public static final ApiError TRIAL_CALC_DATA_NOT_FOUND = new ApiError("TRIAL_CALC_DATA_NOT_FOUND", 15000, "未找到符合条件的试算数据");
    public static final ApiError TRIAL_CALC_DATA_INCONSISTENT = new ApiError("TRIAL_CALC_DATA_INCONSISTENT", 15001, "所选数据存在SKU、店铺或试算开始时间不一致，无法进行对比");
    public static final ApiError TRIAL_CALC_HISTORY_SALES_INCONSISTENT = new ApiError("TRIAL_CALC_HISTORY_SALES_INCONSISTENT", 15002, "所选数据历史销量不一致，无法进行比较");
    public static final ApiError TRIAL_CALC_START_DATE_AFTER_NOW_FORBIDDEN = new ApiError("TRIAL_CALC_START_DATE_AFTER_NOW_FORBIDDEN", 15003, "试算开始日期不能晚于当前日期");
    public static final ApiError TRIAL_CALC_END_DATE_BEFORE_START_FORBIDDEN = new ApiError("TRIAL_CALC_END_DATE_BEFORE_START_FORBIDDEN", 15004, "试算结束日期不能早于试算开始日期");
    public static final ApiError TRIAL_CALC_DATE_RANGE_EXCEEDS_ONE_YEAR = new ApiError("TRIAL_CALC_DATE_RANGE_EXCEEDS_ONE_YEAR", 15005, "试算开始日期与结束日期间隔不能超过一年");
    public static final ApiError TRIAL_CALC_END_DATE_AFTER_MIN_FORBIDDEN = new ApiError("TRIAL_CALC_END_DATE_AFTER_MIN_FORBIDDEN", 15006, "结束日期不能晚于所选数据中的最小试算结束日期");
    public static final ApiError TRIAL_CALC_START_DATE_BEFORE_MIN_FORBIDDEN = new ApiError("TRIAL_CALC_START_DATE_BEFORE_MIN_FORBIDDEN", 15007, "开始日期不能早于所选数据的试算开始日期");
    public static final ApiError TRIAL_CALC_TASK_SIZE_EXCEEDS_LIMIT = new ApiError("TRIAL_CALC_TASK_SIZE_EXCEEDS_LIMIT", 15008, "单模板按SKU×店铺维度计算，最多支持999999条任务");

    static ApiError[] values() {
        return new ApiError[]{
                CUSTOMER_ADDRESS_NOT_MATCH,
                CUSTOMER_GROUP_REQUIRED,
                CUSTOMER_GROUP_NAME_DUPLICATE,
                CUSTOMER_GROUP_IN_USE_DELETE_FORBIDDEN,
                CUSTOMER_DEFAULT_CONTACT_LIMIT,
                CUSTOMER_DEFAULT_ADDRESS_LIMIT,
                CUSTOMER_DEFAULT_BANK_LIMIT,
                CUSTOMER_NOT_FOUND,
                CUSTOMER_ADDRESS_IN_USE_DELETE_FORBIDDEN,
                CUSTOMER_DISABLE_FORBIDDEN,
                CUSTOMER_SKU_INTERVAL_OVERLAP,
                CUSTOMER_NAME_DUPLICATE,
                CUSTOMER_NAME_RELATE_SHOP_FORBIDDEN,
                SALES_DEMAND_NOT_FOUND,
                SALES_DEMAND_DETAIL_NOT_FOUND,
                SALES_DEMAND_SKU_QTY_EXCEEDS,
                SALES_DEMAND_ALREADY_PUSHED_SUBCONTRACT_REVERSE_FORBIDDEN,
                SHOP_NAME_EXISTS,
                SHOP_AUTH_SHIPMENT_ERROR,
                SHOP_NOT_AUTH_ERROR,
                SHOP_FBA_MARKETPLACE_DISABLED,
                SHOP_AUTHORIZE_CODE_REQUIRED,
                SHOP_AUTHORIZE_FAILED,
                SHOP_TOKEN_FETCH_FAILED,
                SHOP_ALREADY_AUTHORIZED,
                SHOP_LISTING_NOT_FOUND,
                SHOP_AUTHORIZE_ERROR,
                SHOP_WALMART_ID_REQUIRED,
                SHOP_WALMART_CLIENT_ID_REQUIRED,
                SHOP_WALMART_CLIENT_SECRET_REQUIRED,
                SHOP_AUTH_REQUIRED,
                SHOP_EXIST,
                SHOP_COUNTRY_EXIST,
                SHOP_NOT_FOUND,
                SHOP_NOT_EXIST_NO_PERMISSION,
                SHOP_PARAM_AUTHORIZE_FAILED,
                SHOP_DELETE_ONLY_DISABLED,
                SHOP_INVOICE_NOT_BIND_COMPANY,
                SHOP_COUNTRY_CODE_REQUIRED,
                SHOP_TIKTOK_AUTHORIZED_SHOPS_EMPTY,
                SHOP_TIKTOK_REGION_NOT_MATCH,
                SO_DELIVERY_DETAIL_SKU_NOT_EXIST,
                SO_DELIVERY_NOTICE_NOT_EXIST,
                SO_DELIVERY_SALES_ORDER_PUSH_STOCK_APPLY_QTY_EXCEEDS,
                SO_RETURN_PUSH_ALLOWED_SOURCE_SALES_RETURN_RECEIPT_ONLY,
                SO_CLOSED_PRODUCT_EXISTS_CANNOT_PUSH,
                SO_OUTBOUND_NOT_FOUND,
                SO_RETURN_RECEIPT_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                SO_DEMAND_REQ_PUSHED_DELIVERY_PICKLIST_LOCKED,
                SO_NOTICE_PUSHED_OUTBOUND_PICKLIST_LOCKED,
                SO_PICKLIST_NOT_FOUND_FOR_SO,
                SO_PICKLIST_EXISTS_FORBID_VOID_DELETE,
                SO_OUTBOUND_QTY_EXCEEDS_ORDER,
                SO_DEMAND_REQ_ALREADY_PUSHED_DELIVERY,
                SO_NOT_APPROVED_PUSH_OUTBOUND_FORBIDDEN,
                SO_PICKLIST_ALREADY_EXISTS,
                SO_PICKLIST_TOTAL_QTY_ZERO_FORBIDDEN,
                SO_PICKLIST_DETAIL_NOT_FOUND_FOR_SO,
                SO_SKU_FULLY_ALLOCATED,
                SO_WAVE_GENERATED_SHORTAGE_AUTO,
                SO_ABNORMAL_ORDER_HANDLE_ALLOWED_ONLY,
                SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN,
                SO_ABNORMAL_ORDER_AUTO_DELIVERY_FORBIDDEN_FOR_SO,
                SO_WAVE_GEN_ALLOWED_PENDING_NON_INTERCEPT,
                SO_WAVE_NO_AND_SKU_REQUIRED,
                SO_WAVE_EXIST_MANAGED_AND_NORMAL_ORDER,
                SO_WAVE_NO_AND_BASKET_REQUIRED,
                SO_WAVE_NO_REQUIRED,
                SO_WAVE_SKU_NOT_FOUND,
                SO_WAVE_SAME_WAREHOUSE_REQUIRED,
                SO_IN_AUTO_REPLENISH_PRINT_FORBIDDEN,
                SO_IN_PICK_OR_SUSPENDED_INTERCEPT_FORBIDDEN,
                SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FAIL,
                SO_ABNORMAL_ORDER_CANCEL_DELIVERY_INTERCEPT_FORBIDDEN,
                SO_SKU_NOT_PICKED_CANNOT_ALLOCATE,
                SO_NOTICE_PICK_QTY_EXCEEDS_DELIVERY,
                SO_BUNDLE_PICK_QTY_RATIO_INVALID,
                SO_OUTBOUND_ALREADY_PUSHED,
                SO_DEMAND_REQ_COMPLETED_PICKLIST_LOCKED,
                SO_PICKLIST_GENERATED_TRANSFER_REVERSE_FORBIDDEN,
                SO_NOTICE_ALREADY_PICKLIST_LOCKED,
                SO_PICKLIST_PUSHED_DELIVERY_WH_MOVE_LOCKED,
                SO_THIRD_PARTY_ORDER_MODIFY_FORBIDDEN,
                SO_NOTICE_APPROVED_PICKLIST_GEN_FORBIDDEN,
                SO_NOTICE_APPROVED_PICKLIST_MODIFY_DELETE_FORBIDDEN,
                SO_RETURN_INSTOCK_NOT_GENERATE,
                SO_DELIVERY_STATUS_NOT_SUPPORT_MANUAL_SHIP_FLAG,
                SO_DELIVERY_B2C_NOT_EXISTS,
                SO_DELIVERY_STATUS_NOT_ALLOW_MANUAL_DELIVERY,
                SO_DELIVERY_PLATFORM_ERROR_MSG,
                SO_RETURN_RECEIVE_SKU_NOT_EXIST,
                SO_PUSH_MACHINE,
                SO_APPROVE_ONLY_CAN_UPLOAD_PACKING,
                SO_DELIVERY_ALREADY_PUSHED_NOT_UPDATE_MAPPING,
                SO_B2C_DELIVERY_ALREADY_EXIST,
                SO_INTERCEPTED_STATUS_NOT_UPDATE,
                SO_FULLY_MANAGED_ORDER_NOT_NEED_MANUAL_SHIP,
                SO_NOT_FULLY_MANAGED_ORDER_NOT_PRINT_SKU_BARCODE,
                SO_FULLY_MANAGED_AND_B2C_NOT_PRINT_TOGETHER,
                SO_B2C_DELIVERY_STATUS_NOT_ALLOW_MANUAL_SHIP_FLAG,
                SO_MANUAL_SHIP_ALLOWED_APPROVED_PENDING_ONLY,
                SO_DISTRIBUTION_MANUAL_SHIP_ALLOWED,
                SO_STATUS_NOT_WAVE_CANNOT_PRINT_PICKING,
                SO_CONTAIN_NON_FULLY_MANAGED_ORDER_NOT_PRINT_BARCODE,
                SO_DELIVERY_NOTICE_WAREHOUSE_REQUIRED,
                SO_B2C_SHIPMENT_WAREHOUSE_REQUIRED,
                SO_OUTSTOCK_BILL_COST_NOT_DISAPPROVE,
                SO_OUTBOUND_RECORD_NOT_FOUND,
                SO_DELIVERY_NOTICE_RECORD_NOT_FOUND,
                SO_B2C_DELIVERY_FINISH_PRINT_ONLY,
                SO_B2C_DELIVERY_NOT_FINISH_PRINT_ONLY,
                SO_B2B_ORDER_PACK_ONLY,
                SO_UNPICKED_QUANTITY_SHORTAGE,
                SO_DELIVERY_NOTICE_DETAIL_NOT_EXIST,
                SO_DETAIL_NOT_EXIST,
                SO_OUTBOUND_B2B_REQUIRED,
                SO_DELIVERY_REQUIRED_PENDING_NOT_APPROVED,
                SO_TRANSFER_NOT_RETRY_OUTSTOCK,
                SO_OUTSTOCK_UPDATE_ALLOWED_WAIT_NOTIFY_ONLY,
                SO_OUTSTOCK_PUSH_ALLOWED_FLAG_ONLY,
                SO_NOT_FOUND,
                SO_OUTBOUND_PUSH_REVERSE_FORBIDDEN,
                SO_DELIVERY_RETURN_QTY_EXCEEDS_OUTBOUND,
                SO_DELIVERY_QTY_EXCEEDS_SALES,
                SO_DELIVERY_QTY_EXCEEDS_FROZEN,
                SO_DELIVERY_RETURN_NOTICE_PUSH_REVERSE_FORBIDDEN,
                SO_DELIVERY_RETURN_SIGN_PUSH_REVERSE_FORBIDDEN,
                SO_DELIVERY_RETURN_ORDER_APPROVED_REQUIRED_NOTICE,
                SO_DETAIL_NOT_FOUND,
                SO_DELETE_FORBIDDEN,
                SO_DELIVERY_RETURN_SIGN_QTY_EXCEEDS,
                SO_DELIVERY_RETURN_NOTICE_APPROVED_REQUIRED_PUSH,
                SO_EXPORT_CONTRACT_INVOICE_ALLOWED,
                SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND,
                SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS,
                SO_NOT_APPROVED_CANNOT_PUSH_STOCKREQ,
                SO_DELIVERY_RETURN_ORDER_INBOUND_QTY_EXCEEDS,
                SO_DELIVERY_QTY_GT_REQUIRED_QTY,
                SO_DELIVERY_QTY_GT_AVAILABLE_QTY,
                SO_DELIVERY_OUTBOUND_DETAIL_REQUIRED,
                SO_DELIVERY_QTY_GT_STOCK,
                SO_DELIVERY_RETURN_SIGN_APPROVED_REQUIRED_PUSH_INBOUND,
                SO_CHANGE_APPROVED_REQUIRED,
                SO_CHANGE_NOT_FOUND,
                SO_CHANGE_DETAIL_NOT_FOUND,
                SO_ASSOCIATED_DOC_DELETE_FORBIDDEN,
                SO_CHANGE_CONFLICT,
                SO_ASSOCIATED_DOC_REVERSE_FORBIDDEN,
                SO_CHANGE_IN_PROGRESS,
                SO_APPROVED_REQUIRED_PUSH,
                SO_NOT_VOID_REQUIRED_PUSH,
                SO_DELIVERY_RETURN_SIGN_TOTAL_QTY_EXCEEDS,
                SO_CHANGE_IN_REVERSE_FORBIDDEN,
                SO_CHANGE_QTY_LT_DELIVERY_NOTICE,
                SO_B2C_DELIVERY_K3_CLOUD_OUTBOUND_WAREHOUSE_NOT_FOUND,
                SO_B2C_DELIVERY_K3_CLOUD_RETURN_WAREHOUSE_NOT_FOUND,
                SO_B2C_DELIVERY_K3_CLOUD_RETURN_SKU_NOT_FOUND,
                SO_DEMAND_DATE_GT_ORDER_DATE,
                SO_B2C_NOT_FOUND,
                SO_B2C_WILDBERRIES_NOT_ALLOWED,
                SO_B2C_PLATFORM_SHOP_REQUIRED,
                SO_B2C_LOGISTICS_NOT_FOUND,
                SO_B2C_RECEIVER_NOT_FOUND,
                SO_B2C_EXTEND_NOT_FOUND,
                SO_B2C_DETAIL_NOT_FOUND,
                SO_B2C_DETAIL_IMPORT_FAILED,
                SO_B2C_DETAIL_SAVE_OR_UPDATE_FAILED,
                SO_B2C_DELIVERY_WAREHOUSE_CONFLICT,
                SO_B2C_SKU_INVENTORY_NOT_FOUND,
                SO_B2C_MERGE_PLATFORM_CONFLICT,
                SO_B2C_MERGE_SHOP_CONFLICT,
                SO_B2C_MERGE_CURRENCY_CONFLICT,
                SO_B2C_MERGE_LOGISTICS_METHOD_CONFLICT,
                SO_B2C_MERGE_BUYER_CONFLICT,
                SO_B2C_MERGE_RECEIVER_CONFLICT,
                SO_B2C_MERGE_ADDRESS_CONFLICT,
                SO_B2C_MERGE_WAREHOUSE_CONFLICT,
                SO_B2C_CANCEL_MERGE_NOT_SUPPORTED,
                SO_B2C_MERGE_NOT_SPLIT,
                SO_B2C_SPLIT_QTY_EXCEEDS,
                SO_B2C_NOT_SPLIT_ORDER,
                SO_B2C_SPLIT_ORDER_REQUIRED,
                SO_B2C_CATEGORY_NOT_FOUND,
                SO_B2C_CANCEL_NOT_SUPPORTED,
                SO_B2C_REVERSE_VOID_FORBIDDEN,
                SO_B2C_MERGE_SIZE_REQUIRED,
                SO_B2C_CHILD_NOT_FOUND,
                SO_B2C_LOGISTICS_CODE_ONLY_DISTRIBUTION,
                SO_B2C_SUBMIT_DELIVERY_NOT_ALLOWED,
                SO_B2C_DISTRIBUTION_STATUS_REQUIRED,
                SO_B2C_CANCEL_MERGE_STATUS_LIMIT,
                SO_B2C_LOGISTICS_METHOD_NOT_FOUND,
                SO_B2C_CUSTOMER_NOT_FOUND,
                SO_B2C_FINANCE_NOT_FOUND,
                SO_B2C_NOT_NEED_MERGE,
                SO_NOT_APPROVED_PUSH_FORBIDDEN,
                SO_B2C_PLATFORM_ORDER_VOIDED,
                SO_B2C_ORDER_VOIDED,
                SO_B2C_LOGISTICS_CHANNEL_REQUIRED,
                SO_B2C_LOGISTICS_CHANNEL_AND_NO_REQUIRED,
                SO_B2C_UPDATE_CATEGORY_FORBIDDEN,
                SO_B2C_UPDATE_REMARK_FORBIDDEN,
                SO_B2C_NOT_APPROVED_DISTRIBUTION_FORBIDDEN,
                SO_B2C_SUBMIT_FORBIDDEN_WHEN_FROZEN_OR_VOIDED,
                SO_B2C_SPLIT_FORBIDDEN_BY_STATUS,
                SO_B2C_MERGE_FORBIDDEN_BY_STATUS,
                SO_B2C_SHOPEE_SPLIT_FORBIDDEN,
                SO_B2C_MERCADO_SPLIT_FORBIDDEN,
                SO_B2C_MERGE_FBA_FORBIDDEN,
                SO_B2C_MERGE_CAINIAO_FORBIDDEN,
                SO_B2C_MERGE_TAX_ORDER_FORBIDDEN,
                SO_B2C_SHOPEE_MERGE_FORBIDDEN,
                SO_B2C_MERCADO_MERGE_FORBIDDEN,
                SO_B2C_TIKTOK_MERGE_FORBIDDEN,
                SO_B2C_PAYMENT_REQUIRED,
                SO_DELIVERY_WAREHOUSE_NOT_FOUND,
                SO_B2C_ORDER_FETCH_FAILED,
                SO_B2C_WAREHOUSE_NOT_FOUND,
                SO_B2C_ALREADY_MERGED_OR_SPLIT,
                SO_DELIVERY_STATUS_REQUIRED_FOR_INTERCEPT,
                SO_DELIVERY_ALREADY_INTERCEPTED,
                SO_CHANGE_ALREADY_TERMINATED,
                SO_PUSH_MACHINE_DATA_NOT_FOUND,
                SO_B2C_REVERSE_APPROVE_STATUS_LIMIT,
                SO_B2C_APPROVED_REQUIRED_FOR_DELIVERY,
                SO_OUTBOUND_EXISTS_MAPPING_UPDATE_FORBIDDEN,
                SO_DELIVERY_EXISTS_MAPPING_UPDATE_FORBIDDEN,
                SO_B2C_SOURCE_ONLY_MAPPING_UPDATE_ALLOWED,
                SO_B2C_DISTRIBUTION_DECLARE_STATUS_INVALID,
                SO_B2C_DECLARE_ALREADY_EXISTS,
                SO_B2C_DECLARE_INFO_NOT_FOUND,
                SO_B2C_TIKTOK_SPLIT_FORBIDDEN_WITH_REASON,
                SO_B2C_TIKTOK_SPLIT_SKU_FORBIDDEN,
                SO_B2C_TIKTOK_SPLIT_FAILED,
                SO_B2C_SPLIT_BY_WAREHOUSE_FORBIDDEN,
                SO_B2C_LOGISTICS_PLATFORM_REQUIRED,
                SO_B2C_MULTI_CHANNEL_FORBIDDEN,
                SO_B2C_SPLIT_KOL_FORBIDDEN,
                SO_B2C_MAGALU_SPLIT_FORBIDDEN,
                SO_B2C_MAGALU_MERGE_FORBIDDEN,
                SO_DETAIL_SKU_ALL_EMPTY_FORBIDDEN,
                SO_REPLACE_SKU_STATUS_INVALID,
                SO_B2B_SALESMAN_CHANGE,
                SO_REFUND_ORDER_DETAIL,
                SO_PLATFORM_ORDER_MERGE_TOO_LONG,
                SO_DELIVERY_AUTO_SUBMIT_OPTION_LIMIT,
                SO_OUTBOUND_ALREADY_GENERATED_TERMINATE_FORBIDDEN,
                SO_PICKLIST_NOT_PROCESSED_FORBIDDEN,
                SO_RETURN_NOTICE_SKU_NOT_FOUND,
                SO_RETURN_SIGN_SKU_NOT_FOUND,
                SO_RETURN_QTY_EXCEEDS_EXPECTED,
                SO_RETURN_NOTICE_DETAIL_REQUIRED,
                SO_RETURN_SIGN_DETAIL_REQUIRED,
                SO_RETURN_INBOUND_DETAIL_REQUIRED,
                SO_PRICE_EXPIRE_BEFORE_EFFECTIVE,
                SO_PRICE_DATE_RANGE_OVERLAP,
                SO_PRICE_NOT_FOUND,
                SO_PRICE_DETAIL_NOT_FOUND,
                SO_ORG_NOT_REPEAT,
                SO_PRICE_INTERVAL_INVALID,
                SO_ALREADY_REF_DOWNSTREAM_BILL_FORBIDDEN,
                SO_RETURN_DETAIL_SKU_NOT_FOUND,
                SO_B2C_ADD_GIFT_STATUS_FORBIDDEN,
                SO_WDT_SALES_RAW_TRADE_PUSHSELF,
                SO_LOGISTICS_WAYBILL_NOT_OBTAINED,
                SO_THIRD_DELIVERY_INTERCEPT_ONLY_WAIT_SHIPPED,
                SO_THIRD_DELIVERY_MANUAL_ONLY_B2B_DISABLED,
                SO_THIRD_DELIVERY_ONLY_WAIT_SHIPPED,
                SO_THIRD_DELIVERY_GENERATE_OUTSTOCK_ONLY_SHIPPED,
                SO_THIRD_DELIVERY_DELETE_ONLY_FAILED_OR_CANCELED,
                SO_B2C_GET_EXCHANGE_RATE_FAILED,
                SO_RETURN_EXCHANGE_RATE_REQUIRED,
                SO_RETURN_RECEIVE_QTY_INVALID,
                SO_RETURN_RECEIVE_AMOUNT_MISSING,
                SO_RETURN_INSTOCK_IMPORT_DUPLICATE_CODE,
                SO_RETURN_INSTOCK_IMPORT_BATCH_ABORT,
                SO_RETURN_INSTOCK_IMPORT_PERSIST_FAILED,
                SO_RETURN_INSTOCK_IMPORT_CODE_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_UPDATE_STATUS_INVALID,
                SO_RETURN_INSTOCK_IMPORT_SOURCE_TYPE_FORBIDDEN,
                SO_RETURN_INSTOCK_IMPORT_CUSTOMER_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_CUSTOMER_CHANGE_FORBIDDEN,
                SO_RETURN_INSTOCK_IMPORT_INVENTORY_ORG_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_CURRENCY_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_BILL_TYPE_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_BILL_DATE_INVALID,
                SO_RETURN_INSTOCK_IMPORT_RETURN_QTY_INVALID,
                SO_RETURN_INSTOCK_IMPORT_ADD_CUSTOMER_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_ADD_CURRENCY_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_ADD_SKU_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_LOCATION_NOT_FOUND,
                SO_RETURN_INSTOCK_IMPORT_TASK_CREATE_FAILED,
                SO_RETURN_INSTOCK_IMPORT_TASK_FAILED,
                SO_RETURN_INSTOCK_IMPORT_CUSTOMER_DUPLICATE,
                SO_RETURN_INSTOCK_IMPORT_SKU_OCCUPY_FAILED,
                SO_B2C_NOT_OUTBOUND_DETAIL_WAREHOUSE_UPDATE_FAILED,
                SO_B2C_NOT_OUTBOUND_LOGISTICS_UPDATE_FAILED,
                SO_B2C_NOT_OUTBOUND_STATUS_UPDATE_FAILED,
                SO_CHANGE_DELETE_ALL_DETAIL_FORBIDDEN,
                SO_OUTSTOCK_AMOUNT_MISMATCH_SUBMIT,
                SO_OUTSTOCK_AMOUNT_MISMATCH_APPROVE,
                SO_OUTSTOCK_UPSTREAM_AMOUNT_CHECK_UNAVAILABLE,
                SO_B2C_DELIVERY_TRANSFER_NOT_PERSISTED,
                SO_B2C_DELIVERY_TRANSFER_NOT_APPROVED,
                SO_B2C_DELIVERY_MULTI_WAREHOUSE_NOT_SUPPORTED,
                SO_DELIVERY_NOTICE_CUSTOMER_COUNTRY_INCONSISTENT,
                TRIAL_CALC_DATA_NOT_FOUND,
                TRIAL_CALC_DATA_INCONSISTENT,
                TRIAL_CALC_HISTORY_SALES_INCONSISTENT,
                TRIAL_CALC_START_DATE_AFTER_NOW_FORBIDDEN,
                TRIAL_CALC_END_DATE_BEFORE_START_FORBIDDEN,
                TRIAL_CALC_DATE_RANGE_EXCEEDS_ONE_YEAR,
                TRIAL_CALC_END_DATE_AFTER_MIN_FORBIDDEN,
                TRIAL_CALC_START_DATE_BEFORE_MIN_FORBIDDEN,
                TRIAL_CALC_TASK_SIZE_EXCEEDS_LIMIT,
        };
    }
}
