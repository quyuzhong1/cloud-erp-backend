package com.common.core.enums;

/**
 * Fms service error constants.
 */
public final class ApiErrorFms {

    private ApiErrorFms() {
    }

    public static final ApiError FIN_INVOICE_NOT_FOUND = new ApiError("FIN_INVOICE_NOT_FOUND", 14000, "发票信息不存在");
    public static final ApiError FIN_INVOICE_OPERATION_NOT_ALLOWED = new ApiError("FIN_INVOICE_OPERATION_NOT_ALLOWED", 14001, "发票未开票成功，不支持当前操作");
    public static final ApiError FIN_INVOICE_NOT_REQUIRED_ONLY_PENDING_OR_FAILED = new ApiError("FIN_INVOICE_NOT_REQUIRED_ONLY_PENDING_OR_FAILED", 14002, "仅待开票或开票失败的订单支持设置为无需开票");
    public static final ApiError FIN_INVOICE_NFE_ONLY_SUPPORTED = new ApiError("FIN_INVOICE_NFE_ONLY_SUPPORTED", 14003, "仅NF-e类型发票支持该操作");
    public static final ApiError FIN_SKU_INVOICE_TAX_INFO_NOT_FOUND = new ApiError("FIN_SKU_INVOICE_TAX_INFO_NOT_FOUND", 14004, "平台SKU【{0}】、店铺【{1}】未找到对应税务信息");
    public static final ApiError FIN_INVOICE_NFE_CANCEL_FAILED = new ApiError("FIN_INVOICE_NFE_CANCEL_FAILED", 14005, "更新NF-e发票CCE失败，原因：{0}");
    public static final ApiError FIN_COMPANY_TOKEN_NOT_FOUND = new ApiError("FIN_COMPANY_TOKEN_NOT_FOUND", 14006, "公司token不存在");
    public static final ApiError FIN_INVOICE_NFE_UPDATE_CCE_FAILED = new ApiError("FIN_INVOICE_NFE_UPDATE_CCE_FAILED", 14007, "更新Cce发票失败，原因：{0}");
    public static final ApiError FIN_INVOICE_UPLOAD_FILE_NOT_FOUND = new ApiError("FIN_INVOICE_UPLOAD_FILE_NOT_FOUND", 14008, "发票上传文件不存在");
    public static final ApiError FIN_INVOICE_NFE_JSON_PARSE_FAILED = new ApiError("FIN_INVOICE_NFE_JSON_PARSE_FAILED", 14009, "NF-e发票创建时JSON解析失败");
    public static final ApiError FIN_INVOICE_CREATING_REGENERATE_FORBIDDEN = new ApiError("FIN_INVOICE_CREATING_REGENERATE_FORBIDDEN", 14010, "订单正在开票处理中，不支持重新生成发票");
    public static final ApiError FIN_INVOICE_NFE_RETURN_FAILED = new ApiError("FIN_INVOICE_NFE_RETURN_FAILED", 14011, "退票发票失败，原因：{0}");
    public static final ApiError FIN_INVOICE_NFE_VOID_FAILED = new ApiError("FIN_INVOICE_NFE_VOID_FAILED", 14012, "作废发票失败，原因：{0}");
    public static final ApiError FIN_RECONCILIATION_NOT_FOUND = new ApiError("FIN_RECONCILIATION_NOT_FOUND", 14013, "对账单不存在");
    public static final ApiError FIN_RECONCILIATION_DETAIL_NOT_FOUND = new ApiError("FIN_RECONCILIATION_DETAIL_NOT_FOUND", 14014, "对账明细不存在");
}
