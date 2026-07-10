package com.common.core.enums;

/**
 * Srm service error constants.
 */
public final class ApiErrorSrm {

    private ApiErrorSrm() {
    }

    public static final ApiError SUPPLIER_LEVEL_NAME_EXISTS = new ApiError("SUPPLIER_LEVEL_NAME_EXISTS", 8500, "供应商等级名不能重复");
    public static final ApiError SUPPLIER_LEVEL_IN_USE = new ApiError("SUPPLIER_LEVEL_IN_USE", 8501, "存在已被供应商引用的等级");
    public static final ApiError SUPPLIER_STAGE_NOT_FOUND = new ApiError("SUPPLIER_STAGE_NOT_FOUND", 8502, "供应商阶段不存在");
    public static final ApiError SUPPLIER_STAGE_INVALID = new ApiError("SUPPLIER_STAGE_INVALID", 8503, "供应商当前阶段有误");
    public static final ApiError SUPPLIER_NAME_EMPTY = new ApiError("SUPPLIER_NAME_EMPTY", 8504, "供应商名称不能为空");
    public static final ApiError SUPPLIER_NAME_EXISTS = new ApiError("SUPPLIER_NAME_EXISTS", 8505, "供应商名称不能重复");
    public static final ApiError SUPPLIER_NOT_FOUND = new ApiError("SUPPLIER_NOT_FOUND", 8506, "供应商不存在");
    public static final ApiError SUPPLIER_INFO_REQUIRED = new ApiError("SUPPLIER_INFO_REQUIRED", 8507, "供应商信息不能为空");
    public static final ApiError SUPPLIER_CONTACT_NOT_FOUND = new ApiError("SUPPLIER_CONTACT_NOT_FOUND", 8508, "未找到供应商联系人");
    public static final ApiError SUPPLIER_DEFAULT_CONTACT_EXCEEDS_ONE = new ApiError("SUPPLIER_DEFAULT_CONTACT_EXCEEDS_ONE", 8509, "供应商默认联系人只能有一个");
    public static final ApiError SUPPLIER_QUALIFICATION_DATE_INVALID = new ApiError("SUPPLIER_QUALIFICATION_DATE_INVALID", 8510, "资质起始有效期不能大于资质截止有效期");
    public static final ApiError SUPPLIER_CERT_NAME_EXISTS = new ApiError("SUPPLIER_CERT_NAME_EXISTS", 8511, "证照名称已存在");
    public static final ApiError SUPPLIER_UN_APPROVE = new ApiError("SUPPLIER_UN_APPROVE", 8512, "供应商未审核");
    public static final ApiError SUPPLIER_DISABLE = new ApiError("SUPPLIER_DISABLE", 8513, "供应商未启用");
    public static final ApiError SUPPLIER_SRM_DISABLE = new ApiError("SUPPLIER_SRM_DISABLE", 8514, "供应商未协同");
    public static final ApiError SUPPLIER_USER_NOT_REL = new ApiError("SUPPLIER_USER_NOT_REL", 8515, "用户未关联供应商");
    public static final ApiError SUPPLIER_DOC_USER_MISMATCH = new ApiError("SUPPLIER_DOC_USER_MISMATCH", 8516, "单据供应商与用户供应商不一致");
    public static final ApiError SUPPLIER_INTERVAL_OVERLAP = new ApiError("SUPPLIER_INTERVAL_OVERLAP", 8517, "该供应商SKU区间存在重叠，不可提交");
    public static final ApiError SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL = new ApiError("SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL", 8518, "存在待对账明细/未确认的对账单，请完成对账后关闭");
    public static final ApiError SUPPLIER_ACCOUNT_NOT_FOUND = new ApiError("SUPPLIER_ACCOUNT_NOT_FOUND", 8519, "没有找到供应商的账户信息");
    public static final ApiError SUPPLIER_REF_WAREHOUSE_EXIST = new ApiError("SUPPLIER_REF_WAREHOUSE_EXIST", 8520, "供应商【{0}】仓库【{1}】仓位【{2}】已存在");
    public static final ApiError SUPPLIER_REF_WAREHOUSE_GLOBAL_EXISTS = new ApiError("SUPPLIER_REF_WAREHOUSE_GLOBAL_EXISTS", 8521, "供应商【{0}】在仓库【{1}】已存在全局仓位");
    public static final ApiError SUPPLIER_REF_WAREHOUSE_GLOBAL_CONFLICT = new ApiError("SUPPLIER_REF_WAREHOUSE_GLOBAL_CONFLICT", 8522, "供应商【{0}】仓库【{1}】全局仓位不能与其他仓位共存");
    public static final ApiError SUPPLIER_CONFIG_ALREADY_EXISTS = new ApiError("SUPPLIER_CONFIG_ALREADY_EXISTS", 8523, "供应商配置信息已存在");
    public static final ApiError SUPPLIER_REF_NOT_FOUND = new ApiError("SUPPLIER_REF_NOT_FOUND", 8524, "查询不到关联供应商");
    public static final ApiError SUPPLIER_MODIFY_FORBIDDEN = new ApiError("SUPPLIER_MODIFY_FORBIDDEN", 8525, "供应商不允许修改");

    static ApiError[] values() {
        return new ApiError[]{
                SUPPLIER_LEVEL_NAME_EXISTS,
                SUPPLIER_LEVEL_IN_USE,
                SUPPLIER_STAGE_NOT_FOUND,
                SUPPLIER_STAGE_INVALID,
                SUPPLIER_NAME_EMPTY,
                SUPPLIER_NAME_EXISTS,
                SUPPLIER_NOT_FOUND,
                SUPPLIER_INFO_REQUIRED,
                SUPPLIER_CONTACT_NOT_FOUND,
                SUPPLIER_DEFAULT_CONTACT_EXCEEDS_ONE,
                SUPPLIER_QUALIFICATION_DATE_INVALID,
                SUPPLIER_CERT_NAME_EXISTS,
                SUPPLIER_UN_APPROVE,
                SUPPLIER_DISABLE,
                SUPPLIER_SRM_DISABLE,
                SUPPLIER_USER_NOT_REL,
                SUPPLIER_DOC_USER_MISMATCH,
                SUPPLIER_INTERVAL_OVERLAP,
                SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL,
                SUPPLIER_ACCOUNT_NOT_FOUND,
                SUPPLIER_REF_WAREHOUSE_EXIST,
                SUPPLIER_REF_WAREHOUSE_GLOBAL_EXISTS,
                SUPPLIER_REF_WAREHOUSE_GLOBAL_CONFLICT,
                SUPPLIER_CONFIG_ALREADY_EXISTS,
                SUPPLIER_REF_NOT_FOUND,
                SUPPLIER_MODIFY_FORBIDDEN,
        };
    }
}
