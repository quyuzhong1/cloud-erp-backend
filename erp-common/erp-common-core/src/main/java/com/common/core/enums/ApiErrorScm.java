package com.common.core.enums;

/**
 * Scm service error constants.
 */
public final class ApiErrorScm {

    private ApiErrorScm() {
    }

    public static final ApiError PO_ITEMS_TO_GENERATE_NOT_FOUND = new ApiError("PO_ITEMS_TO_GENERATE_NOT_FOUND", 9500, "未找到可生成采购订单的采购申请明细");
    public static final ApiError PO_APPLY_NOT_FOUND = new ApiError("PO_APPLY_NOT_FOUND", 9501, "未找到采购申请单");
    public static final ApiError PO_APPLY_DETAIL_NOT_FOUND = new ApiError("PO_APPLY_DETAIL_NOT_FOUND", 9502, "未找到采购申请单明细");
    public static final ApiError PO_APPLY_DETAIL_ALREADY_PUSHED = new ApiError("PO_APPLY_DETAIL_ALREADY_PUSHED", 9503, "采购申请单【{0}】明细SKU【{1}】已下推采购订单");
    public static final ApiError PO_APPLY_QTY_EXCEEDS_PENDING_QTY = new ApiError("PO_APPLY_QTY_EXCEEDS_PENDING_QTY", 9504, "采购申请单【{0}】下级SKU【{1}】采购数量不能大于待申请数量");
    public static final ApiError PO_NOT_FOUND = new ApiError("PO_NOT_FOUND", 9505, "未找到采购订单");
    public static final ApiError PO_DETAIL_NOT_FOUND = new ApiError("PO_DETAIL_NOT_FOUND", 9506, "采购单明细信息不存在");
    public static final ApiError PO_SUPPLIER_INFO_NOT_FOUND = new ApiError("PO_SUPPLIER_INFO_NOT_FOUND", 9507, "未找到采购订单供应商信息");
    public static final ApiError PO_PURCHASE_ORG_NOT_FOUND = new ApiError("PO_PURCHASE_ORG_NOT_FOUND", 9508, "采购组织不存在");
    public static final ApiError PO_RECEIVE_ORG_NOT_FOUND = new ApiError("PO_RECEIVE_ORG_NOT_FOUND", 9509, "收料组织不存在");
    public static final ApiError PO_DELIVERY_WH_REQUIRED = new ApiError("PO_DELIVERY_WH_REQUIRED", 9510, "采购订单【{0}】交货仓库不能为空");
    public static final ApiError PO_ORG_REQUIRED = new ApiError("PO_ORG_REQUIRED", 9511, "采购订单【{0}】收料组织不能为空");
    public static final ApiError PO_SUPPLIER_ACCOUNT_REQUIRED = new ApiError("PO_SUPPLIER_ACCOUNT_REQUIRED", 9512, "采购订单【{0}】供应商账户信息不能为空");
    public static final ApiError PO_CAN_GENERATE_ONLY_WHEN_APPROVED = new ApiError("PO_CAN_GENERATE_ONLY_WHEN_APPROVED", 9513, "已审核数据才能生成采购单");
    public static final ApiError PO_APPLY_APPROVAL_NOT_ALLOWED_ONLY = new ApiError("PO_APPLY_APPROVAL_NOT_ALLOWED_ONLY", 9514, "只有未生成采购订单的申请单才能反审核");
    public static final ApiError PO_SUBMIT_FAILED = new ApiError("PO_SUBMIT_FAILED", 9515, "采购订单提交失败");
    public static final ApiError PO_APPROVE_FAILED = new ApiError("PO_APPROVE_FAILED", 9516, "采购订单审核失败");
    public static final ApiError PO_SUBMIT_OR_REJECT_EXPORT_CONTRACT_FORBIDDEN = new ApiError("PO_SUBMIT_OR_REJECT_EXPORT_CONTRACT_FORBIDDEN", 9517, "待提交和审核不通过采购订单不支持导出采购合同");
    public static final ApiError PO_APPROVED_ONLY_CAN_PUSH_RECEIPT = new ApiError("PO_APPROVED_ONLY_CAN_PUSH_RECEIPT", 9518, "只有已审核采购订单能下推签收单");
    public static final ApiError PO_APPROVED_ONLY_CAN_PUSH_QC_APPLICATION = new ApiError("PO_APPROVED_ONLY_CAN_PUSH_QC_APPLICATION", 9518, "只有已审核采购订单能下推质检申请单");
    public static final ApiError PO_DETAIL_CONFIRM_OR_DELIVER_CAN_PUSH_RECEIPT = new ApiError("PO_DETAIL_CONFIRM_OR_DELIVER_CAN_PUSH_RECEIPT", 9519, "只有已确认或送货中的采购订单明细允许下推签收单");
    public static final ApiError PO_APPROVED_ONLY_CAN_PUSH_INBOUND = new ApiError("PO_APPROVED_ONLY_CAN_PUSH_INBOUND", 9520, "只有已审核采购订单支持下推采购入库单");
    public static final ApiError PO_RECEIVE_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("PO_RECEIVE_ALREADY_PUSHED_REVERSE_FORBIDDEN", 9521, "已存在下推收货单，不支持反审核");
    public static final ApiError PO_INSTOCK_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("PO_INSTOCK_ALREADY_PUSHED_REVERSE_FORBIDDEN", 9522, "已存在下推采购入库单，不支持反审核");
    public static final ApiError PO_END_DELIVERY_ALLOWED_ONLY = new ApiError("PO_END_DELIVERY_ALLOWED_ONLY", 9523, "只有已确认，已拒绝和送货中的采购订单能结束交货");
    public static final ApiError PO_DELIVERY_STATUS_CHANGE_FAILED = new ApiError("PO_DELIVERY_STATUS_CHANGE_FAILED", 9524, "采购订单交货状态变更失败");
    public static final ApiError PO_ALREADY_GENERATED_DELIVERY_CANNOT_CHANGE_WH_OR_SUPPLIER = new ApiError("PO_ALREADY_GENERATED_DELIVERY_CANNOT_CHANGE_WH_OR_SUPPLIER", 9525, "{0}已生成交货单,不可变更仓库和供应商");
    public static final ApiError PO_ALREADY_GENERATED_INBOUND_CANNOT_CHANGE_WH_OR_SUPPLIER = new ApiError("PO_ALREADY_GENERATED_INBOUND_CANNOT_CHANGE_WH_OR_SUPPLIER", 9526, "{0}已生成入库单,不可变更仓库和供应商");
    public static final ApiError PO_CHANGE_NOT_FOUND = new ApiError("PO_CHANGE_NOT_FOUND", 9527, "未找到采购变更单");
    public static final ApiError PO_CHANGE_DETAIL_NOT_FOUND = new ApiError("PO_CHANGE_DETAIL_NOT_FOUND", 9528, "未找到采购变更明细单");
    public static final ApiError PO_NOT_APPROVED_CHANGE_FORBIDDEN = new ApiError("PO_NOT_APPROVED_CHANGE_FORBIDDEN", 9529, "非已审核采购订单不支持变更");
    public static final ApiError PO_SKU_HAS_PUSHED_DOC_CHANGE_DELETE_FORBIDDEN = new ApiError("PO_SKU_HAS_PUSHED_DOC_CHANGE_DELETE_FORBIDDEN", 9530, "SKU【{0}】已存在下推单据，不支持删除变更");
    public static final ApiError PO_SKU_CHANGE_QTY_LESS_THAN_PO_QTY = new ApiError("PO_SKU_CHANGE_QTY_LESS_THAN_PO_QTY", 9531, "SKU【{0}】变更数量【{1}】不能小于关联订单采购数量【{2}】");
    public static final ApiError PO_INSTOCK_NOT_FOUND = new ApiError("PO_INSTOCK_NOT_FOUND", 9532, "未找到采购入库单");
    public static final ApiError PO_INSTOCK_DETAIL_NOT_FOUND = new ApiError("PO_INSTOCK_DETAIL_NOT_FOUND", 9533, "未找到采购入库单明细");
    public static final ApiError PO_INSTOCK_APPROVED_ONLY_CAN_PUSH_RETURN = new ApiError("PO_INSTOCK_APPROVED_ONLY_CAN_PUSH_RETURN", 9534, "仅已审核的采购入库单支持下推采购退货单");
    public static final ApiError PO_INBOUND_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("PO_INBOUND_ALREADY_PUSHED_REVERSE_FORBIDDEN", 9535, "已下推入库单，不允许执行反审核操作");
    public static final ApiError PO_RETURN_NOT_EXISTS = new ApiError("PO_RETURN_NOT_EXISTS", 9536, "未找到采购退货单");
    public static final ApiError PO_RETURN_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("PO_RETURN_ALREADY_PUSHED_REVERSE_FORBIDDEN", 9537, "已存在下推的退货单，不支持反审核操作");
    public static final ApiError PO_RETURN_INBOUND_ALREADY_PUSHED = new ApiError("PO_RETURN_INBOUND_ALREADY_PUSHED", 9538, "已下推退货入库单【{0}】，不允许执行反审核操作");
    public static final ApiError PO_DETAIL_DATE_REQUIRED = new ApiError("PO_DETAIL_DATE_REQUIRED", 9539, "采购订单【{0}】SKU【{1}】预计交货日期不能为空");
    public static final ApiError PO_DATE_INVALID = new ApiError("PO_DATE_INVALID", 9540, "采购订单SKU【{0}】预计交货日期不能小于【{1}】");
    public static final ApiError PO_PRICE_INVALID = new ApiError("PO_PRICE_INVALID", 9541, "采购订单SKU【{0}】单价必须大于0");
    public static final ApiError PO_SKU_PURCHASE_QTY_INVALID = new ApiError("PO_SKU_PURCHASE_QTY_INVALID", 9542, "sku【{0}】采购数量不能小于等于0");
    public static final ApiError PO_ALREADY_QTY_EXCEED = new ApiError("PO_ALREADY_QTY_EXCEED", 9543, "采购数量不能大于待申请数量");
    public static final ApiError PO_CHANGE_QTY_EXCEED = new ApiError("PO_CHANGE_QTY_EXCEED", 9544, "采购变更数量不能大于待申请数量");
    public static final ApiError PO_SUBCONTRACT_ORDER_NOT_FOUND = new ApiError("PO_SUBCONTRACT_ORDER_NOT_FOUND", 9557, "未找到委外订单");
    public static final ApiError PO_SUBCONTRACT_DETAIL_NOT_FOUND = new ApiError("PO_SUBCONTRACT_DETAIL_NOT_FOUND", 9558, "未找到委外订单明细");
    public static final ApiError PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS = new ApiError("PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS", 9559, "委外订单【{0}】明细父级SKU【{1}】数量不能大于{2}");
    public static final ApiError PO_SUBCONTRACT_ALREADY_PUSHED_REVERSE_FORBIDDEN = new ApiError("PO_SUBCONTRACT_ALREADY_PUSHED_REVERSE_FORBIDDEN", 9560, "委外订单【{0}】已下推采购订单【{1}】,不支持反审核");
    public static final ApiError PO_RECONCILIATION_ONLY_PENDING_SUPPLIER_CONFIRM_ALLOWED = new ApiError("PO_RECONCILIATION_ONLY_PENDING_SUPPLIER_CONFIRM_ALLOWED", 9561, "仅【待供方确认】支持此操作");
    public static final ApiError PO_RECONCILIATION_ONLY_PENDING_BUYER_CONFIRM_ALLOWED = new ApiError("PO_RECONCILIATION_ONLY_PENDING_BUYER_CONFIRM_ALLOWED", 9562, "仅【待采方确认】支持此操作");
    public static final ApiError PO_RECONCILIATION_BUYER_CONFIRM_OR_CONFIRMED_ALLOWED = new ApiError("PO_RECONCILIATION_BUYER_CONFIRM_OR_CONFIRMED_ALLOWED", 9563, "仅【待采方确认】或【已确认待完结】支持此操作");
    public static final ApiError PO_RECONCILIATION_DELETE_STATUS_FORBIDDEN = new ApiError("PO_RECONCILIATION_DELETE_STATUS_FORBIDDEN", 9564, "仅【待供方确认】或【待采方确认】支持删除对账单");
    public static final ApiError PO_RECONCILIATION_ONLY_CONFIRMED_RECEIVABLE_ALLOWED = new ApiError("PO_RECONCILIATION_ONLY_CONFIRMED_RECEIVABLE_ALLOWED", 9565, "仅【已确认待完结】支持单据签收");
    public static final ApiError PO_RECONCILIATION_UPDATE_STATUS_FORBIDDEN = new ApiError("PO_RECONCILIATION_UPDATE_STATUS_FORBIDDEN", 9566, "仅【待供方确认】或【待采方确认】支持修改对账单");
    public static final ApiError PO_RECONCILIATION_DATE_RANGE_INVALID = new ApiError("PO_RECONCILIATION_DATE_RANGE_INVALID", 9567, "对账开始时间不能晚于结束时间");
    public static final ApiError PO_HAS_SUPPLIER_DELETE_FORBIDDEN = new ApiError("PO_HAS_SUPPLIER_DELETE_FORBIDDEN", 9568, "采购订单存在对应供应商,不能删除");
    public static final ApiError PO_SUBCONTRACT_DETAIL_PARENT_SKU_NOT_FOUND = new ApiError("PO_SUBCONTRACT_DETAIL_PARENT_SKU_NOT_FOUND", 9569, "未找到委外订单明细父级SKU信息");
    public static final ApiError PO_SUBCONTRACT_DETAIL_CHILD_SKU_NOT_FOUND = new ApiError("PO_SUBCONTRACT_DETAIL_CHILD_SKU_NOT_FOUND", 9570, "未找到委外订单明细子级SKU信息");
    public static final ApiError PO_SUBCONTRACT_NOT_EDITABLE = new ApiError("PO_SUBCONTRACT_NOT_EDITABLE", 9571, "委外采购订单不支持修改");
    public static final ApiError PO_SUBCONTRACT_CHANGE_DETAIL_PARENT_SKU_NOT_FOUND = new ApiError("PO_SUBCONTRACT_CHANGE_DETAIL_PARENT_SKU_NOT_FOUND", 9572, "未找到委外变更单明细父级SKU信息");
    public static final ApiError PO_SUBCONTRACT_CHANGE_DETAIL_CHILD_SKU_NOT_FOUND = new ApiError("PO_SUBCONTRACT_CHANGE_DETAIL_CHILD_SKU_NOT_FOUND", 9573, "未找到委外变更单明细子级SKU信息");
    public static final ApiError PO_SUBCONTRACT_CHANGE_NOT_FOUND = new ApiError("PO_SUBCONTRACT_CHANGE_NOT_FOUND", 9574, "未找到委外变更单");
    public static final ApiError PO_SUBCONTRACT_CHANGE_DETAIL_NOT_FOUND = new ApiError("PO_SUBCONTRACT_CHANGE_DETAIL_NOT_FOUND", 9575, "未找到委外变更单明细");
    public static final ApiError PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS_REMAIN = new ApiError("PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS_REMAIN", 9576, "父级SKU【{0}】数量不能超过采购申请剩余可下推数量【{1}】");
    public static final ApiError PO_SUBCONTRACT_PURCHASE_QTY_PUSHED_END = new ApiError("PO_SUBCONTRACT_PURCHASE_QTY_PUSHED_END", 9577, "采购量已经下推完毕");
    public static final ApiError PO_SUBCONTRACT_SELECT_COMPOSITE_SKU_TO_GENERATE = new ApiError("PO_SUBCONTRACT_SELECT_COMPOSITE_SKU_TO_GENERATE", 9578, "请选择组合SKU生成委外订单");
    public static final ApiError PO_SUBCONTRACT_PUSH_CHANGE = new ApiError("PO_SUBCONTRACT_PUSH_CHANGE", 9579, "委外订单【{0}】已下推委外变更单【{1}】,不支持反审核");
    public static final ApiError PO_SUBCONTRACT_PUSH_ISSUE = new ApiError("PO_SUBCONTRACT_PUSH_ISSUE", 9580, "委外订单【{0}】已下推委外发料单【{1}】,不支持反审核");
    public static final ApiError PO_ID_REPEAT = new ApiError("PO_ID_REPEAT", 9581, "请选择同一采购订单下明细进行变更");
    public static final ApiError PO_DETAIL_SKU_NOT_EXIST = new ApiError("PO_DETAIL_SKU_NOT_EXIST", 9582, "sku【{0}】在采购单中不存在");
    public static final ApiError PO_INSTOCK_DETAIL_SKU_NOT_EXIST = new ApiError("PO_INSTOCK_DETAIL_SKU_NOT_EXIST", 9583, "sku【{0}】在采购收货单中未找到");
    public static final ApiError PO_PUSH_DOWN_CHANGE_EXISTS = new ApiError("PO_PUSH_DOWN_CHANGE_EXISTS", 9584, "采购订单已下推采购变更单");
    public static final ApiError PO_PUSH_DOWN_DELIVERY_EXISTS = new ApiError("PO_PUSH_DOWN_DELIVERY_EXISTS", 9585, "采购订单已下推送货单");
    public static final ApiError PO_PUSH_DOWN_QC_APPLICATION_EXISTS = new ApiError("PO_PUSH_DOWN_QC_APPLICATION_EXISTS", 9585, "采购订单已下质检申请单【{0}】");
    public static final ApiError PO_SUPPLIER_CONFIRM_NOT_ALLOWED = new ApiError("PO_SUPPLIER_CONFIRM_NOT_ALLOWED", 9586, "采购订单【{0}】未审核完成不支持确认");
    public static final ApiError PO_DETAIL_SUPPLIER_CONFIRM_NOT_ALLOWED = new ApiError("PO_DETAIL_SUPPLIER_CONFIRM_NOT_ALLOWED", 9587, "采购订单【{0}】非待确认不支持确认");
    public static final ApiError PO_SKU_PUSH_DOWN_NOT_ALLOWED = new ApiError("PO_SKU_PUSH_DOWN_NOT_ALLOWED", 9588, "采购订单【{0}】SKU【{1}】非已确认和送货中、已完成不支持下推");
    public static final ApiError PO_CLOSE_REVERSE_NOT_ALLOWED = new ApiError("PO_CLOSE_REVERSE_NOT_ALLOWED", 9589, "已关闭采购订单不支持反审核");
    public static final ApiError PO_NO_SUPPLIER_CONFIRM = new ApiError("PO_NO_SUPPLIER_CONFIRM", 9590, "采购订单【{0}】未关联供应商不支持确认");
    public static final ApiError PO_SUPPLIER_CONFIRM_DIFF = new ApiError("PO_SUPPLIER_CONFIRM_DIFF", 9591, "不能操作其他供应商采购订单【{0}】");
    public static final ApiError PO_DETAIL_DELIVERY_QTY_EXCEEDS = new ApiError("PO_DETAIL_DELIVERY_QTY_EXCEEDS", 9592, "采购订单明细【{0}】送货数量不可超过【待交货量】");
    public static final ApiError PO_CONTRACT_EXPORT_FORBIDDEN_OTHER_SUPPLIER = new ApiError("PO_CONTRACT_EXPORT_FORBIDDEN_OTHER_SUPPLIER", 9593, "非当前供应商【{0}】的采购订单不支持导出采购合同");
    public static final ApiError PO_ADJUST_PRICE_NOT_ALLOWED = new ApiError("PO_ADJUST_PRICE_NOT_ALLOWED", 9594, "采购订单审核中不支持调价");
    public static final ApiError PO_PUSHED_ACCEPT_REVIEW_FORBIDDEN = new ApiError("PO_PUSHED_ACCEPT_REVIEW_FORBIDDEN", 9595, "已存在下推资产验收单，不支持反审核");
    public static final ApiError PO_CHANGE_SKU_QTY_LT_ACCEPTED = new ApiError("PO_CHANGE_SKU_QTY_LT_ACCEPTED", 9596, "sku【{0}】变更数量不能小于已验收数量");
    public static final ApiError PO_SKU_NEW_QTY_LT_ACCEPTED = new ApiError("PO_SKU_NEW_QTY_LT_ACCEPTED", 9597, "sku【{0}】的新采购数量不能小于已验收数量");
    public static final ApiError PO_RETURN_DATA_NOT_FOUND = new ApiError("PO_RETURN_DATA_NOT_FOUND", 9598, "未找到对应退货数据");
    public static final ApiError PO_RECEIPT_NOT_FOUND = new ApiError("PO_RECEIPT_NOT_FOUND", 9599, "收货单不存在或已删除");
    public static final ApiError PO_RECEIPT_QTY_EXCEEDS_ALLOWED = new ApiError("PO_RECEIPT_QTY_EXCEEDS_ALLOWED", 9601, "产品【{0}】的收货数量不能超过可收货数量");
    public static final ApiError PO_RETURN_QTY_EXCEEDS_INBOUND = new ApiError("PO_RETURN_QTY_EXCEEDS_INBOUND", 9602, "【{0}】的退货数量不能大于已入库数量");
    public static final ApiError PO_INBOUND_EXISTS_REVOKE_FORBIDDEN = new ApiError("PO_INBOUND_EXISTS_REVOKE_FORBIDDEN", 9603, "存在有效的入库单记录，不允许撤销操作");
    public static final ApiError PO_RETURN_EXISTS_REVOKE_FORBIDDEN = new ApiError("PO_RETURN_EXISTS_REVOKE_FORBIDDEN", 9604, "存在有效的退货单记录，不允许撤销操作");
    public static final ApiError PO_RETURN_QTY_EXCEEDS_RECEIPT = new ApiError("PO_RETURN_QTY_EXCEEDS_RECEIPT", 9606, "【{0}】的退货数量不能大于收货数量");
    public static final ApiError PO_RETURN_TOTAL_QTY_EXCEEDS_INBOUND = new ApiError("PO_RETURN_TOTAL_QTY_EXCEEDS_INBOUND", 9607, "【{0}】退货单的合计数量不能大于入库数量");
    public static final ApiError PO_RETURN_SKU_CLOSE = new ApiError("PO_RETURN_SKU_CLOSE", 9666, "采购订单【{0}】SKU【{1}】非已确认和送货中、已完成不支持质检");
    public static final ApiError PO_PUSH_TOTAL_QTY_EXCEEDS_RECEIPT = new ApiError("PO_PUSH_TOTAL_QTY_EXCEEDS_RECEIPT", 9608, "【{0}】下推的数量合计不能大于收货数量");
    public static final ApiError PO_RECEIPT_QTY_EXCEEDS_UNDELIVERED = new ApiError("PO_RECEIPT_QTY_EXCEEDS_UNDELIVERED", 9610, "【{0}】的收货数量不能大于未交货数量");
    public static final ApiError PO_INSTOCK_REMAIN_QTY_EXCEEDS = new ApiError("PO_INSTOCK_REMAIN_QTY_EXCEEDS", 9612, "采购订单【{0}】SKU【{1}】的剩余可入库数量不能超过【{2}】");
    public static final ApiError PO_INSTOCK_ALREADY_COMPLETED = new ApiError("PO_INSTOCK_ALREADY_COMPLETED", 9613, "采购订单【{0}】SKU【{1}】已完成入库");
    public static final ApiError PO_RETURN_INBOUND_ALLOWED_APPROVED_ONLY = new ApiError("PO_RETURN_INBOUND_ALLOWED_APPROVED_ONLY", 9614, "仅已审核的单据支持下推退货入库单");
    public static final ApiError PO_RETURN_INBOUND_NOT_FOUND = new ApiError("PO_RETURN_INBOUND_NOT_FOUND", 9615, "退货入库单不存在");
    public static final ApiError PO_RETURN_REF_PO_EXISTS = new ApiError("PO_RETURN_REF_PO_EXISTS", 9618, "采购退货单【{0}】已下推采购订单");
    public static final ApiError PO_RETURN_REF_PO_NOT_APPROVED = new ApiError("PO_RETURN_REF_PO_NOT_APPROVED", 9619, "采购退货单【{0}】未审核完成，不支持下推采购订单");
    public static final ApiError PO_RECEIVE_QTY_EXCEEDS_DELIVERY = new ApiError("PO_RECEIVE_QTY_EXCEEDS_DELIVERY", 9620, "收货数量不能大于送货数量");
    public static final ApiError PO_SUBCONTRACT_ISSUE_SUPPLIER_DIFF = new ApiError("PO_SUBCONTRACT_ISSUE_SUPPLIER_DIFF", 9621, "委外发料单明细对应的供应商【{0}】必须一致");
    public static final ApiError PO_SUBCONTRACT_RETURN_SUPPLIER_DIFF = new ApiError("PO_SUBCONTRACT_RETURN_SUPPLIER_DIFF", 9622, "委外退料单明细对应的供应商【{0}】必须一致");
    public static final ApiError PO_INSTOCK_PUSH_SUBCONTRACT_ISSUE_EXIST = new ApiError("PO_INSTOCK_PUSH_SUBCONTRACT_ISSUE_EXIST", 9623, "采购入库单已下推委外发料单【{0}】");
    public static final ApiError PO_RETURN_UNIT_PRICE_REQUIRED = new ApiError("PO_RETURN_UNIT_PRICE_REQUIRED", 9624, "无关联采购单时，退款单价不能为空");
    public static final ApiError PO_RETURN_SKU_UNIT_PRICE_REQUIRED = new ApiError("PO_RETURN_SKU_UNIT_PRICE_REQUIRED", 9625, "采购退货单【{0}】在无关联采购时，SKU【{1}】的退款单价不能为空");
    public static final ApiError PO_SUBCONTRACT_ISSUE_NOT_EXIST = new ApiError("PO_SUBCONTRACT_ISSUE_NOT_EXIST", 9626, "委外发料单不存在");
    public static final ApiError PO_SUBCONTRACT_ISSUE_DETAIL_NOT_EXIST = new ApiError("PO_SUBCONTRACT_ISSUE_DETAIL_NOT_EXIST", 9627, "委外发料单明细不存在");
    public static final ApiError PO_SUBCONTRACT_ISSUE_QTY_EXCEED = new ApiError("PO_SUBCONTRACT_ISSUE_QTY_EXCEED", 9628, "委外发料单中SKU【{0}】数量不能大于【{1}】");
    public static final ApiError PO_RETURN_CFG_SETTING_NOT_EXISTS = new ApiError("PO_RETURN_CFG_SETTING_NOT_EXISTS", 9629, "退货配置不存在，请先配置异常处理人");
    public static final ApiError PO_RECEIVE_SHOULD_GENERATE_BY_DELIVERY = new ApiError("PO_RECEIVE_SHOULD_GENERATE_BY_DELIVERY", 9630, "【{0}】已开启系统收货协同，请从送货单下推生成收货单");
    public static final ApiError PO_SUBCONTRACT_RETURN_NOT_EXIST = new ApiError("PO_SUBCONTRACT_RETURN_NOT_EXIST", 9631, "委外退料单不存在");
    public static final ApiError PO_SUBCONTRACT_RETURN_DETAIL_NOT_EXIST = new ApiError("PO_SUBCONTRACT_RETURN_DETAIL_NOT_EXIST", 9632, "委外退料单明细不存在");
    public static final ApiError PO_SUBCONTRACT_RETURN_QTY_EXCEED = new ApiError("PO_SUBCONTRACT_RETURN_QTY_EXCEED", 9633, "委外退料单中SKU【{0}】退料数量【{1}】不能大于可退数量【{2}】");
    public static final ApiError PO_SUBCONTRACT_RETURN_ORDER_REVERSE_FORBIDDEN = new ApiError("PO_SUBCONTRACT_RETURN_ORDER_REVERSE_FORBIDDEN", 9634, "存在有效下推单据【委外退料单{0}】，不支持反审核");
    public static final ApiError PO_ORDER_REVERSE_FORBIDDEN = new ApiError("PO_ORDER_REVERSE_FORBIDDEN", 9635, "存在有效下推单据【采购订单{0}】，不支持反审核");
    public static final ApiError PO_RETURN_ORDER_REVERSE_FORBIDDEN = new ApiError("PO_RETURN_ORDER_REVERSE_FORBIDDEN", 9636, "存在有效下推单据【采购退货单{0}】，不支持反审核");
    public static final ApiError PO_SUBCONTRACT_AND_PO_RETURN_REVERSE_FORBIDDEN = new ApiError("PO_SUBCONTRACT_AND_PO_RETURN_REVERSE_FORBIDDEN", 9637, "存在有效下推单据【委外退料单{0}】【采购退货单{1}】，不支持反审核");
    public static final ApiError PO_INSTOCK_PUSH_PO_RECONCILIATION_EXIST = new ApiError("PO_INSTOCK_PUSH_PO_RECONCILIATION_EXIST", 9648, "采购入库单已生成对账记录，不支持反审核");
    public static final ApiError PO_RETURN_REPLENISH_QTY_CHECK = new ApiError("PO_RETURN_REPLENISH_QTY_CHECK", 9649, "SKU【{0}】补货数量必须大于0");
    public static final ApiError PO_RETURN_DEDUCT_AMOUNT_QTY_CHECK = new ApiError("PO_RETURN_DEDUCT_AMOUNT_QTY_CHECK", 9650, "SKU【{0}】扣款数量必须大于0");
    public static final ApiError PO_RECONCILIATION_NOT_CONFIRMED_FOR_GENERATE = new ApiError("PO_RECONCILIATION_NOT_CONFIRMED_FOR_GENERATE", 9651, "单据单号【{0}】未确认，不支持生成采购对账单");
    public static final ApiError PO_RECONCILIATION_ALREADY_GENERATED = new ApiError("PO_RECONCILIATION_ALREADY_GENERATED", 9652, "单据单号【{0}】已生成采购对账单");
    public static final ApiError PO_RECONCILIATION_DETAIL_DELETE_FORBIDDEN = new ApiError("PO_RECONCILIATION_DETAIL_DELETE_FORBIDDEN", 9653, "单据【{0}】已完成对账，不支持删除对账明细");
    public static final ApiError PO_RECONCILIATION_DETAIL_ALREADY_GENERATED = new ApiError("PO_RECONCILIATION_DETAIL_ALREADY_GENERATED", 9654, "单据单号【{0}】已生成对账明细");
    public static final ApiError PO_RECONCILIATION_REF_RECEIVE_DISAPPROVE_FORBIDDEN = new ApiError("PO_RECONCILIATION_REF_RECEIVE_DISAPPROVE_FORBIDDEN", 9655, "单据单号【{0}】已关联对账单，无法反审核");
    public static final ApiError PO_RECONCILIATION_ONLY_RECEIVED_CANCEL_ALLOWED = new ApiError("PO_RECONCILIATION_ONLY_RECEIVED_CANCEL_ALLOWED", 9656, "仅处于【已收单据】状态的对账单支持取消签收");
    public static final ApiError PO_RECONCILIATION_NOT_REQUIRED_FORBIDDEN = new ApiError("PO_RECONCILIATION_NOT_REQUIRED_FORBIDDEN", 9657, "单据单号【{0}】无需对账，不支持生成对账单");
    public static final ApiError PO_INSTOCK_NOT_APPROVED_RECONCILIATION_DETAIL_FORBIDDEN = new ApiError("PO_INSTOCK_NOT_APPROVED_RECONCILIATION_DETAIL_FORBIDDEN", 9658, "单据未审核，不支持生成待对账明细");
    public static final ApiError PO_INSTOCK_QC_RETURN_RECONCILIATION_DETAIL_FORBIDDEN = new ApiError("PO_INSTOCK_QC_RETURN_RECONCILIATION_DETAIL_FORBIDDEN", 9659, "质检退货单据不支持生成待对账明细");
    public static final ApiError PO_RECONCILIATION_MANUAL_GENERATE_FORBIDDEN = new ApiError("PO_RECONCILIATION_MANUAL_GENERATE_FORBIDDEN", 9660, "当前单据不支持手动生成对账明细");
    public static final ApiError PO_RECONCILIATION_NOT_FOUND = new ApiError("PO_RECONCILIATION_NOT_FOUND", 9661, "采购对账单不存在");
    public static final ApiError PO_RECONCILIATION_DETAIL_NOT_FOUND = new ApiError("PO_RECONCILIATION_DETAIL_NOT_FOUND", 9662, "采购对账明细不存在");
    public static final ApiError PO_RECONCILIATION_DETAIL_SUPPLIER_ORG_MISMATCH = new ApiError("PO_RECONCILIATION_DETAIL_SUPPLIER_ORG_MISMATCH", 9663, "对账单【{0}】新增对账明细的供应商【{1}】与结算组织【{2}】必须保持一致");
    public static final ApiError PO_FRAMEWORK_CONTRACT_ATTACHMENT_REQUIRED = new ApiError("PO_FRAMEWORK_CONTRACT_ATTACHMENT_REQUIRED", 9664, "采购框架合同类型附件不能为空");
    public static final ApiError PO_SUBCONTRACT_ONLY_PUSH_ONE_ORDER = new ApiError("PO_SUBCONTRACT_ONLY_PUSH_ONE_ORDER", 9665, "请选择同一采购退货单下明细进行下推");
    public static final ApiError PO_RETURN_DETAIL_NOT_EXISTS = new ApiError("PO_RETURN_DETAIL_NOT_EXISTS", 9666, "未找到采购退货单明细");
    public static final ApiError PO_RETURN_REPAIR_QTY_NOT_ALLOW_BIGGER_THAN_RETURN_QTY = new ApiError("PO_RETURN_REPAIR_QTY_NOT_ALLOW_BIGGER_THAN_RETURN_QTY", 9667, "SKU【{0}】委外返修数量不能大于采购退货数量");
    public static final ApiError PO_REPAIR_SUBCONTRACT_ORDER_NOT_ALLOW_DISAPPROVE = new ApiError("PO_REPAIR_SUBCONTRACT_ORDER_NOT_ALLOW_DISAPPROVE", 9668, "返修委外订单不允许反审核");
    public static final ApiError PO_RETURN_ONLY_SAME_SUPPLIER = new ApiError("PO_RETURN_ONLY_SAME_SUPPLIER", 9669, "只能选择同一供应商的采购退货订单进行下推");
    public static final ApiError PO_RETURN_ONLY_APPROVED_CONFIRMED = new ApiError("PO_RETURN_ONLY_APPROVED_CONFIRMED", 9670, "只能选择审核通过且已确认的采购退货订单进行下推");
    public static final ApiError PO_RETURN_SKU_EXECUTION_STATUS_CLOSED = new ApiError("PO_RETURN_SKU_EXECUTION_STATUS_CLOSED", 9665, "采购订单【{0}】SKU【{1}】执行状态已关闭，请线下退回");
    public static final ApiError PO_RETURN_NOT_ALLOW_PUSH_DOWN = new ApiError("PO_RETURN_NOT_ALLOW_PUSH_DOWN", 9672, "不同退货方式的采购退货单不允许合并下推委外订单");
    public static final ApiError PO_RECONCILIATION_STATUS_NOT_CONFIRM = new ApiError("PO_RECONCILIATION_STATUS_NOT_CONFIRM", 9673, "单据状态不是【已确认待完结】，不允许上传发票");
    public static final ApiError PO_SUBCONTRACT_REPAIR_QTY_MUST_GT_ZERO = new ApiError("PO_SUBCONTRACT_REPAIR_QTY_MUST_GT_ZERO", 9675, "SKU【{0}】返修数量必须大于0");
    public static final ApiError PO_SUBCONTRACT_REPAIR_SUB_LINE_PRICE_REQUIRED = new ApiError("PO_SUBCONTRACT_REPAIR_SUB_LINE_PRICE_REQUIRED", 9676, "SKU【{0}】委外返修子行价格不能为空");
    public static final ApiError PO_SUBCONTRACT_REPAIR_SUB_LINE_TAX_RATE_OR_CURRENCY_REQUIRED = new ApiError("PO_SUBCONTRACT_REPAIR_SUB_LINE_TAX_RATE_OR_CURRENCY_REQUIRED", 9677, "SKU【{0}】委外返修子行税率或币种不能为空");
    public static final ApiError PO_RECONCILIATION_INVOICE_LIMIT_EXCEEDED = new ApiError("PO_RECONCILIATION_INVOICE_LIMIT_EXCEEDED", 9678, "发票数量不能超过10个");
    public static final ApiError PO_RECONCILIATION_INVOICE_FILE_INVALID = new ApiError("PO_RECONCILIATION_INVOICE_FILE_INVALID", 9679, "发票附件 URL 或文件名不能为空");
    public static final ApiError PO_RECONCILIATION_INVOICE_PDF_ONLY = new ApiError("PO_RECONCILIATION_INVOICE_PDF_ONLY", 9680, "仅支持上传PDF格式的文件");
    public static final ApiError PO_DELIVERY_SKU_UPDATE_FORBIDDEN = new ApiError("PO_DELIVERY_SKU_UPDATE_FORBIDDEN", 92202, "已下推发货通知单的明细，不能修改发货sku");
    public static final ApiError PO_BOX_QTY_LESS_THAN_NOTICE_QTY = new ApiError("PO_BOX_QTY_LESS_THAN_NOTICE_QTY", 92203, "发货箱数不能少于已下推的发货通知单数量");
    public static final ApiError PO_BOX_PER_QTY_GT_ONE_SO_OUTBOUND_FORBIDDEN = new ApiError("PO_BOX_PER_QTY_GT_ONE_SO_OUTBOUND_FORBIDDEN", 92205, "单箱数量>1的销售订单不能下推销售出库单");
    public static final ApiError PO_RECONCILIATION_DETAIL_REF_NOT_FOUND = new ApiError("PO_RECONCILIATION_DETAIL_REF_NOT_FOUND", 96004, "对账单明细不存在");
    public static final ApiError PO_RECONCILIATION_DETAIL_STATUS_UPDATE_FORBIDDEN = new ApiError("PO_RECONCILIATION_DETAIL_STATUS_UPDATE_FORBIDDEN", 94106, "仅待对账或无需对账数据允许状态更新");
    public static final ApiError PO_RECONCILIATION_DETAIL_AUTO_UPDATE_FORBIDDEN = new ApiError("PO_RECONCILIATION_DETAIL_AUTO_UPDATE_FORBIDDEN", 94107, "单号【{0}】无需对账不支持自动更新对账状态");
    public static final ApiError PO_RECONCILIATION_DETAIL_QTY_EXCEEDS_AVAILABLE = new ApiError("PO_RECONCILIATION_DETAIL_QTY_EXCEEDS_AVAILABLE", 94108, "单号【{0}】SKU【{1}】本期对账数量{2}超出可对账数量{3}");
    public static final ApiError PO_RECONCILIATION_DETAIL_ALREADY_IN_RECONCILIATION = new ApiError("PO_RECONCILIATION_DETAIL_ALREADY_IN_RECONCILIATION", 94109, "单号【{0}】SKU【{1}】已加入对账单，不允许重复添加");
    public static final ApiError PO_RECONCILIATION_REMARK_REQUIRED = new ApiError("PO_RECONCILIATION_REMARK_REQUIRED", 96009, "对账单备注不能为空");
    public static final ApiError PURCHASE_PRICE_LIST_NOT_FOUND = new ApiError("PURCHASE_PRICE_LIST_NOT_FOUND", 10000, "采购价目表不存在");
    public static final ApiError PURCHASE_PRICE_NOT_EXIST = new ApiError("PURCHASE_PRICE_NOT_EXIST", 10001, "采购价目表不存在");
    public static final ApiError PURCHASE_PRICE_DETAIL_NOT_FOUND = new ApiError("PURCHASE_PRICE_DETAIL_NOT_FOUND", 10002, "未找到采购价目明细");
    public static final ApiError PURCHASE_PRICE_CHANGE_NOT_FOUND = new ApiError("PURCHASE_PRICE_CHANGE_NOT_FOUND", 10003, "采购价目变更不存在");
    public static final ApiError PURCHASE_PRICE_CHANGE_ALLOWED_APPROVED_ONLY = new ApiError("PURCHASE_PRICE_CHANGE_ALLOWED_APPROVED_ONLY", 10004, "只有采购价目审核通过才能变更");
    public static final ApiError PURCHASE_PRICE_CHANGE_APPROVE_STATUS_INVALID = new ApiError("PURCHASE_PRICE_CHANGE_APPROVE_STATUS_INVALID", 10005, "采购调价表未审核通过不支持调价");
    public static final ApiError PURCHASE_PRICE_CHANGE_ADJUST_NOT_ALLOWED = new ApiError("PURCHASE_PRICE_CHANGE_ADJUST_NOT_ALLOWED", 10006, "该调价表数据非最新报价数据不支持批量调价");
    public static final ApiError PURCHASE_PRICE_QUOTE_QUERY_PARAM_REQUIRED = new ApiError("PURCHASE_PRICE_QUOTE_QUERY_PARAM_REQUIRED", 10007, "请输入采购报价查询条件");
    public static final ApiError PURCHASE_PRICE_HAS_SUPPLIER_DELETE_FORBIDDEN = new ApiError("PURCHASE_PRICE_HAS_SUPPLIER_DELETE_FORBIDDEN", 10008, "采购价目存在对应供应商,不能删除");
    public static final ApiError PURCHASE_PRICE_DATE_INVALID = new ApiError("PURCHASE_PRICE_DATE_INVALID", 10009, "采购价目表SKU【{0}】失效时间不可小于生效时间");
    public static final ApiError PURCHASE_PRICE_DATE_OVERLAP = new ApiError("PURCHASE_PRICE_DATE_OVERLAP", 10010, "采购价目表SKU【{0}】时间区间重叠");
    public static final ApiError PURCHASE_PRICE_SUBMIT_SKU_UN_APPROVE = new ApiError("PURCHASE_PRICE_SUBMIT_SKU_UN_APPROVE", 10011, "SKU【{0}】未审核通过，采购价目表数据不支持提交");
    public static final ApiError PURCHASE_PRICE_SKU_NOT_FOUND = new ApiError("PURCHASE_PRICE_SKU_NOT_FOUND", 10012, "SKU【{0}】未找到数量【{1}】的供应商报价信息");
    public static final ApiError PURCHASE_PRICE_SKU_PRICE_NOT_FOUND = new ApiError("PURCHASE_PRICE_SKU_PRICE_NOT_FOUND", 10013, "sku【{0}】未找到价目表");
    public static final ApiError PURCHASE_PRICE_SKU_PRICE_ZERO = new ApiError("PURCHASE_PRICE_SKU_PRICE_ZERO", 10014, "SKU【{0}】价格不能为零");
    public static final ApiError PURCHASE_PRICE_ORG_NOT_REPEAT = new ApiError("PURCHASE_PRICE_ORG_NOT_REPEAT", 10015, "只有相同的采购组织可以批量变更报价");

    static ApiError[] values() {
        return new ApiError[]{
                PO_ITEMS_TO_GENERATE_NOT_FOUND,
                PO_APPLY_NOT_FOUND,
                PO_APPLY_DETAIL_NOT_FOUND,
                PO_APPLY_DETAIL_ALREADY_PUSHED,
                PO_APPLY_QTY_EXCEEDS_PENDING_QTY,
                PO_NOT_FOUND,
                PO_DETAIL_NOT_FOUND,
                PO_SUPPLIER_INFO_NOT_FOUND,
                PO_PURCHASE_ORG_NOT_FOUND,
                PO_RECEIVE_ORG_NOT_FOUND,
                PO_DELIVERY_WH_REQUIRED,
                PO_ORG_REQUIRED,
                PO_SUPPLIER_ACCOUNT_REQUIRED,
                PO_CAN_GENERATE_ONLY_WHEN_APPROVED,
                PO_APPLY_APPROVAL_NOT_ALLOWED_ONLY,
                PO_SUBMIT_FAILED,
                PO_APPROVE_FAILED,
                PO_SUBMIT_OR_REJECT_EXPORT_CONTRACT_FORBIDDEN,
                PO_APPROVED_ONLY_CAN_PUSH_RECEIPT,
                PO_APPROVED_ONLY_CAN_PUSH_QC_APPLICATION,
                PO_DETAIL_CONFIRM_OR_DELIVER_CAN_PUSH_RECEIPT,
                PO_APPROVED_ONLY_CAN_PUSH_INBOUND,
                PO_RECEIVE_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                PO_INSTOCK_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                PO_END_DELIVERY_ALLOWED_ONLY,
                PO_DELIVERY_STATUS_CHANGE_FAILED,
                PO_ALREADY_GENERATED_DELIVERY_CANNOT_CHANGE_WH_OR_SUPPLIER,
                PO_ALREADY_GENERATED_INBOUND_CANNOT_CHANGE_WH_OR_SUPPLIER,
                PO_CHANGE_NOT_FOUND,
                PO_CHANGE_DETAIL_NOT_FOUND,
                PO_NOT_APPROVED_CHANGE_FORBIDDEN,
                PO_SKU_HAS_PUSHED_DOC_CHANGE_DELETE_FORBIDDEN,
                PO_SKU_CHANGE_QTY_LESS_THAN_PO_QTY,
                PO_INSTOCK_NOT_FOUND,
                PO_INSTOCK_DETAIL_NOT_FOUND,
                PO_INSTOCK_APPROVED_ONLY_CAN_PUSH_RETURN,
                PO_INBOUND_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                PO_RETURN_NOT_EXISTS,
                PO_RETURN_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                PO_RETURN_INBOUND_ALREADY_PUSHED,
                PO_DETAIL_DATE_REQUIRED,
                PO_DATE_INVALID,
                PO_PRICE_INVALID,
                PO_SKU_PURCHASE_QTY_INVALID,
                PO_ALREADY_QTY_EXCEED,
                PO_CHANGE_QTY_EXCEED,
                PO_SUBCONTRACT_ORDER_NOT_FOUND,
                PO_SUBCONTRACT_DETAIL_NOT_FOUND,
                PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS,
                PO_SUBCONTRACT_ALREADY_PUSHED_REVERSE_FORBIDDEN,
                PO_RECONCILIATION_ONLY_PENDING_SUPPLIER_CONFIRM_ALLOWED,
                PO_RECONCILIATION_ONLY_PENDING_BUYER_CONFIRM_ALLOWED,
                PO_RECONCILIATION_BUYER_CONFIRM_OR_CONFIRMED_ALLOWED,
                PO_RECONCILIATION_DELETE_STATUS_FORBIDDEN,
                PO_RECONCILIATION_ONLY_CONFIRMED_RECEIVABLE_ALLOWED,
                PO_RECONCILIATION_UPDATE_STATUS_FORBIDDEN,
                PO_RECONCILIATION_DATE_RANGE_INVALID,
                PO_HAS_SUPPLIER_DELETE_FORBIDDEN,
                PO_SUBCONTRACT_DETAIL_PARENT_SKU_NOT_FOUND,
                PO_SUBCONTRACT_DETAIL_CHILD_SKU_NOT_FOUND,
                PO_SUBCONTRACT_NOT_EDITABLE,
                PO_SUBCONTRACT_CHANGE_DETAIL_PARENT_SKU_NOT_FOUND,
                PO_SUBCONTRACT_CHANGE_DETAIL_CHILD_SKU_NOT_FOUND,
                PO_SUBCONTRACT_CHANGE_NOT_FOUND,
                PO_SUBCONTRACT_CHANGE_DETAIL_NOT_FOUND,
                PO_SUBCONTRACT_PARENT_SKU_QTY_EXCEEDS_REMAIN,
                PO_SUBCONTRACT_PURCHASE_QTY_PUSHED_END,
                PO_SUBCONTRACT_SELECT_COMPOSITE_SKU_TO_GENERATE,
                PO_SUBCONTRACT_PUSH_CHANGE,
                PO_SUBCONTRACT_PUSH_ISSUE,
                PO_ID_REPEAT,
                PO_DETAIL_SKU_NOT_EXIST,
                PO_INSTOCK_DETAIL_SKU_NOT_EXIST,
                PO_PUSH_DOWN_CHANGE_EXISTS,
                PO_PUSH_DOWN_DELIVERY_EXISTS,
                PO_PUSH_DOWN_QC_APPLICATION_EXISTS,
                PO_SUPPLIER_CONFIRM_NOT_ALLOWED,
                PO_DETAIL_SUPPLIER_CONFIRM_NOT_ALLOWED,
                PO_SKU_PUSH_DOWN_NOT_ALLOWED,
                PO_CLOSE_REVERSE_NOT_ALLOWED,
                PO_NO_SUPPLIER_CONFIRM,
                PO_SUPPLIER_CONFIRM_DIFF,
                PO_DETAIL_DELIVERY_QTY_EXCEEDS,
                PO_CONTRACT_EXPORT_FORBIDDEN_OTHER_SUPPLIER,
                PO_ADJUST_PRICE_NOT_ALLOWED,
                PO_PUSHED_ACCEPT_REVIEW_FORBIDDEN,
                PO_CHANGE_SKU_QTY_LT_ACCEPTED,
                PO_SKU_NEW_QTY_LT_ACCEPTED,
                PO_RETURN_DATA_NOT_FOUND,
                PO_RECEIPT_NOT_FOUND,
                PO_RECEIPT_QTY_EXCEEDS_ALLOWED,
                PO_RETURN_QTY_EXCEEDS_INBOUND,
                PO_INBOUND_EXISTS_REVOKE_FORBIDDEN,
                PO_RETURN_EXISTS_REVOKE_FORBIDDEN,
                PO_RETURN_QTY_EXCEEDS_RECEIPT,
                PO_RETURN_TOTAL_QTY_EXCEEDS_INBOUND,
                PO_RETURN_SKU_CLOSE,
                PO_PUSH_TOTAL_QTY_EXCEEDS_RECEIPT,
                PO_RECEIPT_QTY_EXCEEDS_UNDELIVERED,
                PO_INSTOCK_REMAIN_QTY_EXCEEDS,
                PO_INSTOCK_ALREADY_COMPLETED,
                PO_RETURN_INBOUND_ALLOWED_APPROVED_ONLY,
                PO_RETURN_INBOUND_NOT_FOUND,
                PO_RETURN_REF_PO_EXISTS,
                PO_RETURN_REF_PO_NOT_APPROVED,
                PO_RECEIVE_QTY_EXCEEDS_DELIVERY,
                PO_SUBCONTRACT_ISSUE_SUPPLIER_DIFF,
                PO_SUBCONTRACT_RETURN_SUPPLIER_DIFF,
                PO_INSTOCK_PUSH_SUBCONTRACT_ISSUE_EXIST,
                PO_RETURN_UNIT_PRICE_REQUIRED,
                PO_RETURN_SKU_UNIT_PRICE_REQUIRED,
                PO_SUBCONTRACT_ISSUE_NOT_EXIST,
                PO_SUBCONTRACT_ISSUE_DETAIL_NOT_EXIST,
                PO_SUBCONTRACT_ISSUE_QTY_EXCEED,
                PO_RETURN_CFG_SETTING_NOT_EXISTS,
                PO_RECEIVE_SHOULD_GENERATE_BY_DELIVERY,
                PO_SUBCONTRACT_RETURN_NOT_EXIST,
                PO_SUBCONTRACT_RETURN_DETAIL_NOT_EXIST,
                PO_SUBCONTRACT_RETURN_QTY_EXCEED,
                PO_SUBCONTRACT_RETURN_ORDER_REVERSE_FORBIDDEN,
                PO_ORDER_REVERSE_FORBIDDEN,
                PO_RETURN_ORDER_REVERSE_FORBIDDEN,
                PO_SUBCONTRACT_AND_PO_RETURN_REVERSE_FORBIDDEN,
                PO_INSTOCK_PUSH_PO_RECONCILIATION_EXIST,
                PO_RETURN_REPLENISH_QTY_CHECK,
                PO_RETURN_DEDUCT_AMOUNT_QTY_CHECK,
                PO_RECONCILIATION_NOT_CONFIRMED_FOR_GENERATE,
                PO_RECONCILIATION_ALREADY_GENERATED,
                PO_RECONCILIATION_DETAIL_DELETE_FORBIDDEN,
                PO_RECONCILIATION_DETAIL_ALREADY_GENERATED,
                PO_RECONCILIATION_REF_RECEIVE_DISAPPROVE_FORBIDDEN,
                PO_RECONCILIATION_ONLY_RECEIVED_CANCEL_ALLOWED,
                PO_RECONCILIATION_NOT_REQUIRED_FORBIDDEN,
                PO_INSTOCK_NOT_APPROVED_RECONCILIATION_DETAIL_FORBIDDEN,
                PO_INSTOCK_QC_RETURN_RECONCILIATION_DETAIL_FORBIDDEN,
                PO_RECONCILIATION_MANUAL_GENERATE_FORBIDDEN,
                PO_RECONCILIATION_NOT_FOUND,
                PO_RECONCILIATION_DETAIL_NOT_FOUND,
                PO_RECONCILIATION_DETAIL_SUPPLIER_ORG_MISMATCH,
                PO_FRAMEWORK_CONTRACT_ATTACHMENT_REQUIRED,
                PO_SUBCONTRACT_ONLY_PUSH_ONE_ORDER,
                PO_RETURN_DETAIL_NOT_EXISTS,
                PO_RETURN_REPAIR_QTY_NOT_ALLOW_BIGGER_THAN_RETURN_QTY,
                PO_REPAIR_SUBCONTRACT_ORDER_NOT_ALLOW_DISAPPROVE,
                PO_RETURN_ONLY_SAME_SUPPLIER,
                PO_RETURN_ONLY_APPROVED_CONFIRMED,
                PO_RETURN_SKU_EXECUTION_STATUS_CLOSED,
                PO_RETURN_NOT_ALLOW_PUSH_DOWN,
                PO_RECONCILIATION_STATUS_NOT_CONFIRM,
                PO_SUBCONTRACT_REPAIR_QTY_MUST_GT_ZERO,
                PO_SUBCONTRACT_REPAIR_SUB_LINE_PRICE_REQUIRED,
                PO_SUBCONTRACT_REPAIR_SUB_LINE_TAX_RATE_OR_CURRENCY_REQUIRED,
                PO_RECONCILIATION_INVOICE_LIMIT_EXCEEDED,
                PO_RECONCILIATION_INVOICE_FILE_INVALID,
                PO_RECONCILIATION_INVOICE_PDF_ONLY,
                PO_DELIVERY_SKU_UPDATE_FORBIDDEN,
                PO_BOX_QTY_LESS_THAN_NOTICE_QTY,
                PO_BOX_PER_QTY_GT_ONE_SO_OUTBOUND_FORBIDDEN,
                PO_RECONCILIATION_DETAIL_REF_NOT_FOUND,
                PO_RECONCILIATION_DETAIL_STATUS_UPDATE_FORBIDDEN,
                PO_RECONCILIATION_DETAIL_AUTO_UPDATE_FORBIDDEN,
                PO_RECONCILIATION_DETAIL_QTY_EXCEEDS_AVAILABLE,
                PO_RECONCILIATION_DETAIL_ALREADY_IN_RECONCILIATION,
                PO_RECONCILIATION_REMARK_REQUIRED,
                PURCHASE_PRICE_LIST_NOT_FOUND,
                PURCHASE_PRICE_NOT_EXIST,
                PURCHASE_PRICE_DETAIL_NOT_FOUND,
                PURCHASE_PRICE_CHANGE_NOT_FOUND,
                PURCHASE_PRICE_CHANGE_ALLOWED_APPROVED_ONLY,
                PURCHASE_PRICE_CHANGE_APPROVE_STATUS_INVALID,
                PURCHASE_PRICE_CHANGE_ADJUST_NOT_ALLOWED,
                PURCHASE_PRICE_QUOTE_QUERY_PARAM_REQUIRED,
                PURCHASE_PRICE_HAS_SUPPLIER_DELETE_FORBIDDEN,
                PURCHASE_PRICE_DATE_INVALID,
                PURCHASE_PRICE_DATE_OVERLAP,
                PURCHASE_PRICE_SUBMIT_SKU_UN_APPROVE,
                PURCHASE_PRICE_SKU_NOT_FOUND,
                PURCHASE_PRICE_SKU_PRICE_NOT_FOUND,
                PURCHASE_PRICE_SKU_PRICE_ZERO,
                PURCHASE_PRICE_ORG_NOT_REPEAT,
        };
    }
}
