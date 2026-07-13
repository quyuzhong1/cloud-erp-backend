package com.common.core.enums;

/**
 * Plm service error constants.
 */
public final class ApiErrorPlm {

    private ApiErrorPlm() {
    }

    public static final ApiError BOM_REQUIRED = new ApiError("BOM_REQUIRED", 6000, "bom不能为空");
    public static final ApiError BOM_CHANGING = new ApiError("BOM_CHANGING", 6001, "bom信息变更中");
    public static final ApiError BOM_NOT_FOUND = new ApiError("BOM_NOT_FOUND", 6002, "未找到选择的BOM信息");
    public static final ApiError BOM_CHILD_NOT_FOUND = new ApiError("BOM_CHILD_NOT_FOUND", 6003, "未找到BOM子件");
    public static final ApiError BOM_CHILD_NOT_FOUND_PARENT = new ApiError("BOM_CHILD_NOT_FOUND_PARENT", 6004, "父级SKU【{0}】未找到BOM子件");
    public static final ApiError BOM_CONTAIN = new ApiError("BOM_CONTAIN", 6005, "BOM【{0}】子级SKU已包含SKU【{1}】");
    public static final ApiError BOM_PARENT_SKU_REPEAT = new ApiError("BOM_PARENT_SKU_REPEAT", 6006, "父级sku【{0}】已生成BOM");
    public static final ApiError BOM_SKU_REPEAT = new ApiError("BOM_SKU_REPEAT", 6007, "父级sku【{0}】和子级sku不能重复");
    public static final ApiError BOM_COMB_SKU_EXISTS = new ApiError("BOM_COMB_SKU_EXISTS", 6008, "组合产品SKU【{0}】已存在");
    public static final ApiError BOM_COMB_STATE_INVALID = new ApiError("BOM_COMB_STATE_INVALID", 6009, "组合产品状态有误");
    public static final ApiError BOM_COMB_NAME_MISMATCH = new ApiError("BOM_COMB_NAME_MISMATCH", 6010, "组合产品SKU【{0}】对应名称不一致");
    public static final ApiError BOM_COMB_EXPORT_FORBIDDEN = new ApiError("BOM_COMB_EXPORT_FORBIDDEN", 6011, "组合产品【{0}】已存在不支持导入");
    public static final ApiError BOM_COMB_NOT_FOUND = new ApiError("BOM_COMB_NOT_FOUND", 6012, "组合产品不存在");
    public static final ApiError BOM_COMB_SKU_NOT_CHINESE = new ApiError("BOM_COMB_SKU_NOT_CHINESE", 6013, "组合产品【{0}】不能输入中文");
    public static final ApiError BOM_COMB_SKU_UNAPPROVED = new ApiError("BOM_COMB_SKU_UNAPPROVED", 6014, "组合产品SKU【{0}】未审核成功");
    public static final ApiError BOM_COMB_CHILD_REPEAT = new ApiError("BOM_COMB_CHILD_REPEAT", 6015, "组合产品子SKU【{0}】不能重复");
    public static final ApiError BOM_FOR_MABANG_EXIST = new ApiError("BOM_FOR_MABANG_EXIST", 6016, "sku【{0}】在马帮的BOM中不存在！");
    public static final ApiError MOULD_NOT_EXIST = new ApiError("MOULD_NOT_EXIST", 6500, "模具档案不存在");
    public static final ApiError MOULD_FILE_AUDITED_ONLY = new ApiError("MOULD_FILE_AUDITED_ONLY", 6501, "只允许选择已审核的模具档案批量关联SKU");
    public static final ApiError MOULD_REF_SKU_EXISTS = new ApiError("MOULD_REF_SKU_EXISTS", 6502, "模具档案已存在关联SKU");
    public static final ApiError MOULD_NOTICE_PURCHASE_ONLY = new ApiError("MOULD_NOTICE_PURCHASE_ONLY", 6503, "只有未生成模具采购订单的通知单单才能反审核");
    public static final ApiError MOULD_NOTICE_NOT_FOUND = new ApiError("MOULD_NOTICE_NOT_FOUND", 6504, "未找到开模通知单");
    public static final ApiError MOULD_NOTICE_DETAIL_NOT_FOUND = new ApiError("MOULD_NOTICE_DETAIL_NOT_FOUND", 6505, "未找到开模通知单明细");
    public static final ApiError MOULD_NOTICE_NO_PUSHABLE = new ApiError("MOULD_NOTICE_NO_PUSHABLE", 6506, "未找到可以下推的开模通知单");
    public static final ApiError MOULD_NOTICE_QTY_EXCEED = new ApiError("MOULD_NOTICE_QTY_EXCEED", 6507, "开模通知单【{0}】,资产【{1}】采购数量不能大于待申请数量");
    public static final ApiError MOULD_NOT_APPROVED = new ApiError("MOULD_NOT_APPROVED", 6508, "模具未审核通过");
    public static final ApiError MOULD_LIFESPAN_TOO_SMALL = new ApiError("MOULD_LIFESPAN_TOO_SMALL", 6509, "寿命数量不能小于预警寿命数量");
    public static final ApiError MOULD_RETURN_EXISTS = new ApiError("MOULD_RETURN_EXISTS", 6510, "模具档案已存在返还策略");
    public static final ApiError MOULD_ALERT_EXISTS = new ApiError("MOULD_ALERT_EXISTS", 6511, "模具档案已存在预警策略");
    public static final ApiError MOULD_COST_REQUIRED = new ApiError("MOULD_COST_REQUIRED", 6512, "模具成本必填，请填写模具成本");
    public static final ApiError MOULD_NOTICE_ALREADY_PUSHED = new ApiError("MOULD_NOTICE_ALREADY_PUSHED", 6513, "开模通知单已下推模具采购单");
    public static final ApiError MOULD_PURCHASE_ALREADY_CHANGED = new ApiError("MOULD_PURCHASE_ALREADY_CHANGED", 6514, "模具采购订单已下推模具采购变更单");
    public static final ApiError MOULD_PURCHASE_ORDER_NOT_FOUND = new ApiError("MOULD_PURCHASE_ORDER_NOT_FOUND", 6515, "未找到模具采购订单");
    public static final ApiError MOULD_PURCHASE_DETAIL_NOT_FOUND = new ApiError("MOULD_PURCHASE_DETAIL_NOT_FOUND", 6516, "未找到模具采购订单明细");
    public static final ApiError MOULD_PURCHASE_NO_PUSHABLE = new ApiError("MOULD_PURCHASE_NO_PUSHABLE", 6517, "未找到可以下推的模具采购订单");
    public static final ApiError MOULD_PURCHASE_NOT_AUDITED_CHANGE_FORBIDDEN = new ApiError("MOULD_PURCHASE_NOT_AUDITED_CHANGE_FORBIDDEN", 6518, "未审核的模具采购单不允许变更");
    public static final ApiError MOULD_PURCHASE_PUSHED_NO_NEW_DETAIL = new ApiError("MOULD_PURCHASE_PUSHED_NO_NEW_DETAIL", 6519, "下推的模具采购单不允许新增明细");
    public static final ApiError MOULD_PURCHASE_DETAIL_MUST_BE_SAME_ORDER = new ApiError("MOULD_PURCHASE_DETAIL_MUST_BE_SAME_ORDER", 6520, "请选择同一模具采购订单下明细进行变更!");
    public static final ApiError MOULD_PURCHASE_SUPPLIER_INFO_MISSING = new ApiError("MOULD_PURCHASE_SUPPLIER_INFO_MISSING", 6521, "未找到模具采购供应商信息");
    public static final ApiError MOULD_PURCHASE_CHANGE_DETAIL_NOT_FOUND = new ApiError("MOULD_PURCHASE_CHANGE_DETAIL_NOT_FOUND", 6522, "未找到模具采购变更单明细");
    public static final ApiError MOULD_PURCHASE_AUDITED_ONLY_FOR_ACCEPTANCE = new ApiError("MOULD_PURCHASE_AUDITED_ONLY_FOR_ACCEPTANCE", 6523, "只有已审核的模具采购单才可以结束验收");
    public static final ApiError MOULD_CODE_ACCEPT_QTY_EXCEED = new ApiError("MOULD_CODE_ACCEPT_QTY_EXCEED", 6524, "模具编码【{0}】验收数量不能超过可验收数量");
    public static final ApiError MOULD_RETURN_EXIST = new ApiError("MOULD_RETURN_EXIST", 6525, "模具【{0}】已生成模具返还策略，无法再次生成");
    public static final ApiError MOULD_ALERT_EXIST = new ApiError("MOULD_ALERT_EXIST", 6526, "模具【{0}】已生成模具预警策略，无法再次生成");
    public static final ApiError PRODUCT_CATEGORY_EXISTS = new ApiError("PRODUCT_CATEGORY_EXISTS", 5000, "产品分类名称已存在");
    public static final ApiError PRODUCT_CATEGORY_HAS_CHILD = new ApiError("PRODUCT_CATEGORY_HAS_CHILD", 5001, "分类下存在子分类，不可删除");
    public static final ApiError PRODUCT_CATEGORY_HAS_PRODUCT = new ApiError("PRODUCT_CATEGORY_HAS_PRODUCT", 5002, "分类下存在产品，请调整分类后删除");
    public static final ApiError PRODUCT_NAME_EXISTS = new ApiError("PRODUCT_NAME_EXISTS", 5003, "产品名称已存在，不可重复提交");
    public static final ApiError PRODUCT_NAME_MISMATCH = new ApiError("PRODUCT_NAME_MISMATCH", 5004, "产品名不一致 无法删除");
    public static final ApiError PRODUCT_NOT_FOUND = new ApiError("PRODUCT_NOT_FOUND", 5005, "产品不存在");
    public static final ApiError PRODUCT_SKU_EXISTS = new ApiError("PRODUCT_SKU_EXISTS", 5006, "SKU已存在，请确保SKU的唯一性");
    public static final ApiError PRODUCT_SPU_EXISTS = new ApiError("PRODUCT_SPU_EXISTS", 5007, "SPU已存在，请确保SPU的唯一性");
    public static final ApiError PRODUCT_CATEGORY_NOT_FOUND = new ApiError("PRODUCT_CATEGORY_NOT_FOUND", 5008, "产品分类不存在");
    public static final ApiError PRODUCT_SKU_NOT_GENERATED = new ApiError("PRODUCT_SKU_NOT_GENERATED", 5009, "尚未生成SKU，请在立项阶段生成SKU后启动");
    public static final ApiError PRODUCT_CATEGORY_CODE_REQUIRED = new ApiError("PRODUCT_CATEGORY_CODE_REQUIRED", 5010, "产品分类代号不能为空");
    public static final ApiError PRODUCT_CATEGORY_CODE_EXISTS = new ApiError("PRODUCT_CATEGORY_CODE_EXISTS", 5011, "产品分类代号已存在");
    public static final ApiError PRODUCT_CATEGORY_CODE_RANGE_INVALID = new ApiError("PRODUCT_CATEGORY_CODE_RANGE_INVALID", 5012, "产品分类代号范围在A-Z区间");
    public static final ApiError PRODUCT_CATEGORY_CODE_NOT_FOUND = new ApiError("PRODUCT_CATEGORY_CODE_NOT_FOUND", 5013, "未找到所选品类的{0}级分类代号");
    public static final ApiError PRODUCT_CATEGORY_CODE_NOT_ALLOWED = new ApiError("PRODUCT_CATEGORY_CODE_NOT_ALLOWED", 5014, "非一二级分类不能添加代号");
    public static final ApiError PRODUCT_VARIANT_COLOR_NOT_FOUND = new ApiError("PRODUCT_VARIANT_COLOR_NOT_FOUND", 5015, "未找到SKU变体颜色信息");
    public static final ApiError PRODUCT_VARIANT_INFO_EMPTY = new ApiError("PRODUCT_VARIANT_INFO_EMPTY", 5016, "变体信息为空");
    public static final ApiError PRODUCT_VARIANT_COLOR_EMPTY = new ApiError("PRODUCT_VARIANT_COLOR_EMPTY", 5017, "变体颜色信息为空");
    public static final ApiError PRODUCT_VARIANT_COLOR_ATTR_EMPTY = new ApiError("PRODUCT_VARIANT_COLOR_ATTR_EMPTY", 5018, "变体颜色属性值为空");
    public static final ApiError PRODUCT_VARIANT_NAME_EXISTS = new ApiError("PRODUCT_VARIANT_NAME_EXISTS", 5019, "变体名已存在，不可重复添加");
    public static final ApiError PRODUCT_VARIANT_VALUE_DUPLICATE = new ApiError("PRODUCT_VARIANT_VALUE_DUPLICATE", 5020, "变体值不可重复，请检查是否有重复的值");
    public static final ApiError PRODUCT_VARIANT_COLOR_CODE_DUPLICATE = new ApiError("PRODUCT_VARIANT_COLOR_CODE_DUPLICATE", 5021, "变体颜色编码不可重复，请检查是否有重复的编码");
    public static final ApiError PRODUCT_VARIANT_TYPE_REF_DELETE_FORBIDDEN = new ApiError("PRODUCT_VARIANT_TYPE_REF_DELETE_FORBIDDEN", 5022, "变体类型已被关联，不可删除");
    public static final ApiError PRODUCT_VARIANT_VALUES_REF_DELETE_FORBIDDEN = new ApiError("PRODUCT_VARIANT_VALUES_REF_DELETE_FORBIDDEN", 5023, "变体值已被关联，不可删除");
    public static final ApiError PRODUCT_VARIANT_VALUE_NOT_FOUND = new ApiError("PRODUCT_VARIANT_VALUE_NOT_FOUND", 5024, "变体类型值不存在或已被删除");
    public static final ApiError PRODUCT_INFO_NOT_FOUND = new ApiError("PRODUCT_INFO_NOT_FOUND", 5025, "产品信息不存在");
    public static final ApiError PRODUCT_ALREADY_INITIATED = new ApiError("PRODUCT_ALREADY_INITIATED", 5026, "已立项的产品不能再次立项");
    public static final ApiError PRODUCT_INITIATE_REQUIRED = new ApiError("PRODUCT_INITIATE_REQUIRED", 5027, "项目未立项");
    public static final ApiError PRODUCT_INITIATE_MISSING_EXISTS = new ApiError("PRODUCT_INITIATE_MISSING_EXISTS", 5028, "存在未立项的项目");
    public static final ApiError PRODUCT_BU_IS_EXISTS_REF = new ApiError("PRODUCT_BU_IS_EXISTS_REF", 5050, "产品BU线已绑定产品");
    public static final ApiError PRODUCT_SKU_REQUIRED = new ApiError("PRODUCT_SKU_REQUIRED", 5029, "sku必须选择一个");
    public static final ApiError PRODUCT_SKU_NOT_FOUND = new ApiError("PRODUCT_SKU_NOT_FOUND", 5030, "SKU不存在");
    public static final ApiError PRODUCT_NOT_FOUND_SKU = new ApiError("PRODUCT_NOT_FOUND_SKU", 5031, "SKU【{0}】不存在");
    public static final ApiError PRODUCT_SKU_RECORD_NOT_FOUND = new ApiError("PRODUCT_SKU_RECORD_NOT_FOUND", 5032, "SKU【{0}】记录不存在");
    public static final ApiError PRODUCT_SKU_APPROVED_REQUIRED = new ApiError("PRODUCT_SKU_APPROVED_REQUIRED", 5033, "请录入已审核的父级SKU");
    public static final ApiError PRODUCT_SKU_CHILD_APPROVED_REQUIRED = new ApiError("PRODUCT_SKU_CHILD_APPROVED_REQUIRED", 5034, "请录入已审核的子级SKU");
    public static final ApiError PRODUCT_SKU_EAN_DUPLICATE = new ApiError("PRODUCT_SKU_EAN_DUPLICATE", 5035, "不可新增相同EAN码");
    public static final ApiError PRODUCT_SKU_CODE_REQUIRED = new ApiError("PRODUCT_SKU_CODE_REQUIRED", 5036, "sku编号必填，请填写sku编号");
    public static final ApiError PRODUCT_SKU_REQUIRED_FOR_OPERATION = new ApiError("PRODUCT_SKU_REQUIRED_FOR_OPERATION", 5037, "SKU为空，不允许进行此操作");
    public static final ApiError PRODUCT_SKU_IN_USE_DELETE_FORBIDDEN = new ApiError("PRODUCT_SKU_IN_USE_DELETE_FORBIDDEN", 5038, "Sku已被其他单据引用不能删除");
    public static final ApiError PRODUCT_SKU_STOCK_REF_CHANGE_FORBIDDEN = new ApiError("PRODUCT_SKU_STOCK_REF_CHANGE_FORBIDDEN", 5039, "SKU存在【{0}】库存，产品属性不允许变更");
    public static final ApiError PRODUCT_EXIST_SKU = new ApiError("PRODUCT_EXIST_SKU", 5040, "SKU【{0}】已存在,不允许反审核");
    public static final ApiError PRODUCT_REQUIRED_FIELDS_INCOMPLETE = new ApiError("PRODUCT_REQUIRED_FIELDS_INCOMPLETE", 5041, "请检查产品必填项是否填写完成");
    public static final ApiError PRODUCT_PM_REQUIRED = new ApiError("PRODUCT_PM_REQUIRED", 5042, "产品经理必填，请填写产品经理");
    public static final ApiError PRODUCT_SALES_METHOD_REQUIRED = new ApiError("PRODUCT_SALES_METHOD_REQUIRED", 5043, "销售方式必填，请填写销售方式");
    public static final ApiError PRODUCT_CATEGORY_REQUIRED = new ApiError("PRODUCT_CATEGORY_REQUIRED", 5044, "产品分类必填，请填写产品分类");
    public static final ApiError PRODUCT_BRAND_REQUIRED = new ApiError("PRODUCT_BRAND_REQUIRED", 5045, "产品品牌必填，请填写产品品牌");
    public static final ApiError PRODUCT_LEVEL_REQUIRED = new ApiError("PRODUCT_LEVEL_REQUIRED", 5046, "产品等级必填，请填写产品等级");
    public static final ApiError PRODUCT_STYLE_NAME_CN_REQUIRED = new ApiError("PRODUCT_STYLE_NAME_CN_REQUIRED", 5047, "产品款名(中文)必填，请填写产品款名(中文)");
    public static final ApiError PRODUCT_STYLE_NAME_EN_REQUIRED = new ApiError("PRODUCT_STYLE_NAME_EN_REQUIRED", 5048, "产品款名(英文)必填，请填写产品款名(英文)");
    public static final ApiError PRODUCT_ATTR_REQUIRED = new ApiError("PRODUCT_ATTR_REQUIRED", 5049, "产品属性必填，请填写产品属性");
    public static final ApiError PRODUCT_SALES_CHANNEL_REQUIRED = new ApiError("PRODUCT_SALES_CHANNEL_REQUIRED", 5050, "销售渠道必填，请填写销售渠道");
    public static final ApiError PRODUCT_COMMISSIONED_DEV_COST_REQUIRED = new ApiError("PRODUCT_COMMISSIONED_DEV_COST_REQUIRED", 5051, "委托开发成本必填，请填写委托开发成本");
    public static final ApiError PRODUCT_TARGET_COST_REQUIRED = new ApiError("PRODUCT_TARGET_COST_REQUIRED", 5052, "目标含税成本必填，请填写目标含税成本");
    public static final ApiError PRODUCT_TARGET_COST_EX_TAX_REQUIRED = new ApiError("PRODUCT_TARGET_COST_EX_TAX_REQUIRED", 5053, "目标不含税成本必填，请填写目标不含税成本");
    public static final ApiError PRODUCT_RETAIL_PRICE_REQUIRED = new ApiError("PRODUCT_RETAIL_PRICE_REQUIRED", 5054, "标准零售价必填，请填写标准零售价");
    public static final ApiError PRODUCT_MASS_PRODUCTION_COST_REQUIRED = new ApiError("PRODUCT_MASS_PRODUCTION_COST_REQUIRED", 5055, "实际量产成本必填，请填写实际量产成本");
    public static final ApiError PRODUCT_TAX_RATE_REQUIRED = new ApiError("PRODUCT_TAX_RATE_REQUIRED", 5056, "税率必填，请填写税率");
    public static final ApiError PRODUCT_ANNUAL_SALES_REQUIRED = new ApiError("PRODUCT_ANNUAL_SALES_REQUIRED", 5057, "年目标销量必填，请填写年目标销量");
    public static final ApiError PRODUCT_MONTHLY_SALES_REQUIRED = new ApiError("PRODUCT_MONTHLY_SALES_REQUIRED", 5058, "目标月销售量必填，请填写目标月销售量");
    public static final ApiError PRODUCT_Q1_SALES_REQUIRED = new ApiError("PRODUCT_Q1_SALES_REQUIRED", 5059, "首季度目标销量必填，请填写首季度目标销量");
    public static final ApiError PRODUCT_ANNUAL_SALES_AMOUNT_REQUIRED = new ApiError("PRODUCT_ANNUAL_SALES_AMOUNT_REQUIRED", 5060, "年目标销售额必填，请填写年目标销售额");
    public static final ApiError PRODUCT_MONTHLY_SALES_AMOUNT_REQUIRED = new ApiError("PRODUCT_MONTHLY_SALES_AMOUNT_REQUIRED", 5061, "目标月销售额必填，请填写目标月销售额");
    public static final ApiError PRODUCT_SALES_COUNTRY_REQUIRED = new ApiError("PRODUCT_SALES_COUNTRY_REQUIRED", 5062, "销售国家必填，请填写销售国家");
    public static final ApiError PRODUCT_IMAGE_COMPLETE_REQUIRED = new ApiError("PRODUCT_IMAGE_COMPLETE_REQUIRED", 5063, "图片是否完成必填，请填写图片是否完成");
    public static final ApiError PRODUCT_VIDEO_COMPLETE_REQUIRED = new ApiError("PRODUCT_VIDEO_COMPLETE_REQUIRED", 5064, "视频是否完成必填，请填写视频是否完成");
    public static final ApiError PRODUCT_SALES_STATUS_REQUIRED = new ApiError("PRODUCT_SALES_STATUS_REQUIRED", 5065, "销售状态必填，请填写销售状态");
    public static final ApiError PRODUCT_SALEABLE_FLAG_REQUIRED = new ApiError("PRODUCT_SALEABLE_FLAG_REQUIRED", 5066, "是否可销售必填，请填写是否可销售");
    public static final ApiError PRODUCT_SALES_PLATFORM_REQUIRED = new ApiError("PRODUCT_SALES_PLATFORM_REQUIRED", 5067, "销售平台必填，请填写销售平台");
    public static final ApiError PRODUCT_COST_INFO_REQUIRED = new ApiError("PRODUCT_COST_INFO_REQUIRED", 5068, "请填写产品成本信息的必填项");
    public static final ApiError PRODUCT_SALES_INFO_REQUIRED = new ApiError("PRODUCT_SALES_INFO_REQUIRED", 5069, "请填写产品销售信息的必填项");
    public static final ApiError PRODUCT_PACK_REQUIRED = new ApiError("PRODUCT_PACK_REQUIRED", 5070, "SKU【{0}】产品包装信息不能为空");
    public static final ApiError PRODUCT_SIZE_REQUIRED = new ApiError("PRODUCT_SIZE_REQUIRED", 5071, "SKU【{0}】包装尺寸不能为空");
    public static final ApiError PRODUCT_BOX_SIZE_REQUIRED = new ApiError("PRODUCT_BOX_SIZE_REQUIRED", 5072, "SKU【{0}】箱规不能为空");
    public static final ApiError PRODUCT_GROSS_WEIGHT_REQUIRED = new ApiError("PRODUCT_GROSS_WEIGHT_REQUIRED", 5073, "SKU【{0}】毛重不能为空");
    public static final ApiError PRODUCT_BOX_WEIGHT_REQUIRED = new ApiError("PRODUCT_BOX_WEIGHT_REQUIRED", 5074, "SKU【{0}】单箱重量不能为空");
    public static final ApiError PRODUCT_NET_WEIGHT_REQUIRED = new ApiError("PRODUCT_NET_WEIGHT_REQUIRED", 5075, "SKU【{0}】净重不能为空");
    public static final ApiError PRODUCT_BOX_QTY_REQUIRED = new ApiError("PRODUCT_BOX_QTY_REQUIRED", 5076, "SKU【{0}】单箱数量不能为空");
    public static final ApiError PRODUCT_ITERATE_SKU_REQUIRED = new ApiError("PRODUCT_ITERATE_SKU_REQUIRED", 5077, "迭代产品不能为空");
    public static final ApiError PRODUCT_CERTIFICATE_EXISTS = new ApiError("PRODUCT_CERTIFICATE_EXISTS", 5078, "SKU【{0}】下已存在证书项目【{1}】的证书");
    public static final ApiError PRODUCT_BASIC_LABEL_EXISTS = new ApiError("PRODUCT_BASIC_LABEL_EXISTS", 5079, "基础标签单{0}已存在");
    public static final ApiError PRODUCT_BASIC_LABEL_LEVEL_NOT_FOUND = new ApiError("PRODUCT_BASIC_LABEL_LEVEL_NOT_FOUND", 5080, "基础标签单级别{0}不存在");
    public static final ApiError PRODUCT_BASIC_LABEL_NAME_EXISTS = new ApiError("PRODUCT_BASIC_LABEL_NAME_EXISTS", 5081, "基础标签单标签名称{0}数据重复");
    public static final ApiError PRODUCT_BASIC_LABEL_SAVE_FAILED = new ApiError("PRODUCT_BASIC_LABEL_SAVE_FAILED", 5082, "基础标签单保存失败");
    public static final ApiError PRODUCT_BASIC_LABEL_REL_SAVE_FAILED = new ApiError("PRODUCT_BASIC_LABEL_REL_SAVE_FAILED", 5083, "产品便签关系保存失败");
    public static final ApiError PRODUCT_INFO_CHANGE_CONTENT_REQUIRED = new ApiError("PRODUCT_INFO_CHANGE_CONTENT_REQUIRED", 5084, "当选择<产品信息变更>时，需选择<变更内容>，最少1项");
    public static final ApiError PRODUCT_TRIAL_DETAIL_NOT_FOUND = new ApiError("PRODUCT_TRIAL_DETAIL_NOT_FOUND", 5085, "试产量产详情不存在");
    public static final ApiError PRODUCT_VENDOR_PRICE_FETCH_FAILED = new ApiError("PRODUCT_VENDOR_PRICE_FETCH_FAILED", 5086, "获取供应商采购价目表失败: sku：{0}，数量：{1}");
    public static final ApiError PRODUCT_VENDOR_PRICE_NOT_SUBMITTED = new ApiError("PRODUCT_VENDOR_PRICE_NOT_SUBMITTED", 5087, "尚未提交供应商采购价目表，请联系采购开发提交后提审:{0}");
    public static final ApiError PRODUCT_VENDOR_PRICE_NOT_FOUND = new ApiError("PRODUCT_VENDOR_PRICE_NOT_FOUND", 5088, "供应商采购价目表不存在，请联系采购开发提交后提审:{0}");
    public static final ApiError PRODUCT_UPLOAD_FORBIDDEN_IN_APPROVING = new ApiError("PRODUCT_UPLOAD_FORBIDDEN_IN_APPROVING", 5089, "审核中不支持上传");
    public static final ApiError PRODUCT_CREATE_FAILED = new ApiError("PRODUCT_CREATE_FAILED", 5090, "产品新增失败");
    public static final ApiError PRODUCT_SKU_NOT_APPROVED_REVOKE_APPROVAL_FORBIDDEN = new ApiError("PRODUCT_SKU_NOT_APPROVED_REVOKE_APPROVAL_FORBIDDEN", 5091, "SKU未审核通过，不支持申请变更");
    public static final ApiError PRODUCT_ATTR_IN_USE_DELETE_FORBIDDEN = new ApiError("PRODUCT_ATTR_IN_USE_DELETE_FORBIDDEN", 5092, "【{0}】属性已被使用，则不允许被删除");
    public static final ApiError PRODUCT_SKU_FIN_CODE_NOT_FOUND = new ApiError("PRODUCT_SKU_FIN_CODE_NOT_FOUND", 5093, "sku【{0}】的财务编码不存在！");
    public static final ApiError PRODUCT_SKU_NOT_COST = new ApiError("PRODUCT_SKU_NOT_COST", 5094, "sku【{0}】未发现成本数据！");
    public static final ApiError PRODUCT_SKU_MABANG_FIN_CODE_NOT_FOUND = new ApiError("PRODUCT_SKU_MABANG_FIN_CODE_NOT_FOUND", 5095, "马帮财务编码：{0}不存在");
    public static final ApiError PRODUCT_SKU_DUPLICATE = new ApiError("PRODUCT_SKU_DUPLICATE", 5096, "存在重复的SKU,不可提交");
    public static final ApiError PRODUCT_SKU_OCCUPY_STATE_UPDATE_FAIL = new ApiError("PRODUCT_SKU_OCCUPY_STATE_UPDATE_FAIL", 5097, "更新sku【{0}】占用状态失败");
    public static final ApiError PRODUCT_SKU_STOCK_EXISTS_SPU_CHANGE_FORBIDDEN = new ApiError("PRODUCT_SKU_STOCK_EXISTS_SPU_CHANGE_FORBIDDEN", 5098, "SKU【{0}】存在库存，禁止变更关联的SPU");
    public static final ApiError PRODUCT_PACKING_SKU_IS_NOT_NULL = new ApiError("PRODUCT_PACKING_SKU_IS_NOT_NULL", 5099, "箱规【{0}】中sku不能为空");
    public static final ApiError PRODUCT_PACKING_SKU_PACK_QTY_IS_NOT_NULL = new ApiError("PRODUCT_PACKING_SKU_PACK_QTY_IS_NOT_NULL", 5100, "箱规【{0}】中sku【{1}】未填写装箱数量");
    public static final ApiError PRODUCT_PACKING_SKU_BOX_QTY_IS_NOT_NULL = new ApiError("PRODUCT_PACKING_SKU_BOX_QTY_IS_NOT_NULL", 5101, "箱规【{0}】中未填写箱数");
    public static final ApiError PRODUCT_SKU_PARAM_NOT_FOUND = new ApiError("PRODUCT_SKU_PARAM_NOT_FOUND", 5102, "SKU【{0}】不存在");
    public static final ApiError PRODUCT_DEV_STATUS_REQUIRED = new ApiError("PRODUCT_DEV_STATUS_REQUIRED", 5103, "产品开发状态必填，请填写产品开发状态");
    public static final ApiError PRODUCT_APP_CATEGORY_CODE_EXISTS = new ApiError("PRODUCT_APP_CATEGORY_CODE_EXISTS", 5104, "应用分类代号已存在");
    public static final ApiError PRODUCT_APP_CATEGORY_NAME_EXISTS = new ApiError("PRODUCT_APP_CATEGORY_NAME_EXISTS", 5105, "应用分类名称已存在");
    public static final ApiError PRODUCT_PROPERTY_ASSET_NOT_EXIST = new ApiError("PRODUCT_PROPERTY_ASSET_NOT_EXIST", 98161, "SKU【{0}】产品属性非资产，与供应商付款条件不一致");
    public static final ApiError PRODUCT_SALES_BATTERY_WEIGHT_NOT_NULL = new ApiError("PRODUCT_SALES_BATTERY_WEIGHT_NOT_NULL", 5106, "产品销售信息电池重量（g）不能为空");
    public static final ApiError PRODUCT_IMG_ATTACHMENT_SAVE_FAILED = new ApiError("PRODUCT_IMG_ATTACHMENT_SAVE_FAILED", 5107, "图片分类附件关联单保存失败");
    public static final ApiError PRODUCT_IMG_ATTACHMENT_NOT_FOUND = new ApiError("PRODUCT_IMG_ATTACHMENT_NOT_FOUND", 5108, "未找到图片分类附件关联单数据");
    public static final ApiError PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_ALL = new ApiError("PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_ALL", 5109, "不能移动到\"所有分类\"");
    public static final ApiError PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_MAIN = new ApiError("PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_MAIN", 5110, "不能移动到\"产品主图\"分类");
    public static final ApiError PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_THUMBNAIL = new ApiError("PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_THUMBNAIL", 5111, "不能移动到\"产品缩略图\"分类");
    public static final ApiError PRODUCT_IMG_DOWNLOAD_MIN_REQUIRED = new ApiError("PRODUCT_IMG_DOWNLOAD_MIN_REQUIRED", 5112, "请至少选择一张图片");
    public static final ApiError PRODUCT_IMG_DOWNLOAD_MAX_LIMIT = new ApiError("PRODUCT_IMG_DOWNLOAD_MAX_LIMIT", 5113, "最多支持50张图片下载");
    public static final ApiError PRODUCT_IMG_DOWNLOAD_NOT_FOUND = new ApiError("PRODUCT_IMG_DOWNLOAD_NOT_FOUND", 5114, "未找到可下载的图片");
    public static final ApiError PRODUCT_CHANGE_SKU_NOT_APPROVE = new ApiError("PRODUCT_CHANGE_SKU_NOT_APPROVE", 5115, "【{0}】只有已审核SKU可以变更");
    public static final ApiError PRODUCT_CHANGE_PRODUCT_SIZE_CHANGE = new ApiError("PRODUCT_CHANGE_PRODUCT_SIZE_CHANGE", 5115, "产品尺寸变更请遵循运费最优尺寸：长≥宽≥高");
    public static final ApiError PRODUCT_CHANGE_BOX_SIZE_CHANGE = new ApiError("PRODUCT_CHANGE_BOX_SIZE_CHANGE", 5115, "箱规尺寸变更请遵循运费最优尺寸：长≥宽≥高");
    public static final ApiError PRODUCT_CHANGE_EXIST = new ApiError("PRODUCT_CHANGE_EXIST", 5115, "已存在未审核的变更单，sku:【{0}】");
    public static final ApiError PRODUCT_RETAIL_PRICE_MISSING = new ApiError("PRODUCT_RETAIL_PRICE_MISSING", 5120, "{0}无零售价，会导致订单无法分摊");
    public static final ApiError PRODUCT_RETAIL_PRICE_MISSING_ZERO = new ApiError("PRODUCT_RETAIL_PRICE_MISSING_ZERO", 5120, "{0}零售价都是0，会导致订单无法分摊");
    public static final ApiError PRODUCT_RETAIL_SKU_MISSING = new ApiError("PRODUCT_RETAIL_SKU_MISSING", 5121, "提取SKU编号失败");
    public static final ApiError PRODUCT_RETAIL_SKU_DUPLICATE = new ApiError("PRODUCT_RETAIL_SKU_DUPLICATE", 5122, "已存在同SKU同币种零售价,不可重复创建");
    public static final ApiError PROJECT_TASK_REQUIRED = new ApiError("PROJECT_TASK_REQUIRED", 4500, "project导入{0}级任务不能为空");
    public static final ApiError PROJECT_TASK_NAME_REQUIRED = new ApiError("PROJECT_TASK_NAME_REQUIRED", 4501, "{0}级任务名称不能为空");
    public static final ApiError PROJECT_TASK_OWNER_REQUIRED = new ApiError("PROJECT_TASK_OWNER_REQUIRED", 4502, "{0}级任务负责人不能为空");
    public static final ApiError PROJECT_OWNER_NOT_FOUND = new ApiError("PROJECT_OWNER_NOT_FOUND", 4503, "未找到project导入{0}级任务负责人");
    public static final ApiError PROJECT_PM_REQUIRED = new ApiError("PROJECT_PM_REQUIRED", 4504, "产品经理不能为空");
    public static final ApiError PROJECT_RND_CENTER_OWNER_REQUIRED = new ApiError("PROJECT_RND_CENTER_OWNER_REQUIRED", 4505, "产品研发中心负责人不能为空");
    public static final ApiError PROJECT_PMO_OWNER_REQUIRED = new ApiError("PROJECT_PMO_OWNER_REQUIRED", 4506, "项目管理部负责人不能为空");
    public static final ApiError PROJECT_DATE_START_AFTER_END = new ApiError("PROJECT_DATE_START_AFTER_END", 4507, "启动日期应当晚于立项日期");
    public static final ApiError PROJECT_DATE_END_AFTER_START = new ApiError("PROJECT_DATE_END_AFTER_START", 4508, "结项日期应当晚于启动日期");
    public static final ApiError PROJECT_STAGE_TASK_EXISTS = new ApiError("PROJECT_STAGE_TASK_EXISTS", 4509, "任务阶段名已存在,不可重复提交");
    public static final ApiError PROJECT_STAGE_TASK_REQUIRED = new ApiError("PROJECT_STAGE_TASK_REQUIRED", 4510, "任务阶段名不能为空");
    public static final ApiError PROJECT_STAGE_INIT_NAME_IMMUTABLE = new ApiError("PROJECT_STAGE_INIT_NAME_IMMUTABLE", 4511, "立项阶段名不可更改");
    public static final ApiError PROJECT_TEMPLATE_EXISTS = new ApiError("PROJECT_TEMPLATE_EXISTS", 4512, "模板名已存在,不可重复提交");
    public static final ApiError PROJECT_TASK_EXISTS = new ApiError("PROJECT_TASK_EXISTS", 4513, "任务名已存在,不可重复提交");
    public static final ApiError PROJECT_ROLE_EXISTS = new ApiError("PROJECT_ROLE_EXISTS", 4514, "项目角色名已存在，不可重复提交");
    public static final ApiError PROJECT_ARCHIVE_FORBIDDEN_NOT_DONE = new ApiError("PROJECT_ARCHIVE_FORBIDDEN_NOT_DONE", 4515, "项目尚未完成，不可归档");
    public static final ApiError PROJECT_STAGE_INIT_DELETE_FORBIDDEN = new ApiError("PROJECT_STAGE_INIT_DELETE_FORBIDDEN", 4516, "立项阶段名不能删除");
    public static final ApiError PROJECT_FIELD_EXISTS = new ApiError("PROJECT_FIELD_EXISTS", 4517, "该项目已有该字段");
    public static final ApiError PROJECT_TASK_HAS_CHILD = new ApiError("PROJECT_TASK_HAS_CHILD", 4518, "该任务存在子任务,不能删除");
    public static final ApiError PROJECT_NOT_FOUND = new ApiError("PROJECT_NOT_FOUND", 4519, "项目不存在");
    public static final ApiError PROJECT_TASK_NOT_FOUND = new ApiError("PROJECT_TASK_NOT_FOUND", 4520, "任务不存在");
    public static final ApiError PROJECT_TASK_BATCH_STATUS_FORBIDDEN = new ApiError("PROJECT_TASK_BATCH_STATUS_FORBIDDEN", 4521, "存在多个任务状态不可批量操作");
    public static final ApiError PROJECT_TASK_PUBLISH_REQUIRED = new ApiError("PROJECT_TASK_PUBLISH_REQUIRED", 4522, "只有待发布才可操作发布任务");
    public static final ApiError PROJECT_TASK_START_REQUIRED = new ApiError("PROJECT_TASK_START_REQUIRED", 4523, "只有待开始或者关闭，才可操作开始任务");
    public static final ApiError PROJECT_TASK_CLOSE_REQUIRED = new ApiError("PROJECT_TASK_CLOSE_REQUIRED", 4524, "只有进行中的任务,才可操作关闭任务");
    public static final ApiError PROJECT_TASK_REVIEW_COMPLETE_FORBIDDEN = new ApiError("PROJECT_TASK_REVIEW_COMPLETE_FORBIDDEN", 4525, "评审任务不可操作完成任务");
    public static final ApiError PROJECT_TASK_PREDECESSOR_UNFINISHED = new ApiError("PROJECT_TASK_PREDECESSOR_UNFINISHED", 4526, "操作失败，前置任务有未完成的任务");
    public static final ApiError PROJECT_TASK_CHILD_UNFINISHED = new ApiError("PROJECT_TASK_CHILD_UNFINISHED", 4527, "操作失败，子任务有未完成的任务");
    public static final ApiError PROJECT_TASK_DOC_CHANGE_FORBIDDEN = new ApiError("PROJECT_TASK_DOC_CHANGE_FORBIDDEN", 4528, "任务未完成 不能变更文档");
    public static final ApiError PROJECT_STAGE_TASK_NOT_FOUND = new ApiError("PROJECT_STAGE_TASK_NOT_FOUND", 4529, "任务阶段不存在");
    public static final ApiError PROJECT_STAGE_INITIATION_DELETE_FORBIDDEN = new ApiError("PROJECT_STAGE_INITIATION_DELETE_FORBIDDEN", 4530, "立项阶段不能删除");
    public static final ApiError PROJECT_STAGE_HAS_TASKS_DELETE_FORBIDDEN = new ApiError("PROJECT_STAGE_HAS_TASKS_DELETE_FORBIDDEN", 4531, "该阶段下已有任务不能删除");
    public static final ApiError PROJECT_TASK_COMPLETE_REQUIRED = new ApiError("PROJECT_TASK_COMPLETE_REQUIRED", 4532, "只有进行中的任务,才可操作完成任务");
    public static final ApiError PROJECT_TASK_DELIVERABLE_UNFINISHED = new ApiError("PROJECT_TASK_DELIVERABLE_UNFINISHED", 4533, "存在输出物尚未完成,请完成后再操作");
    public static final ApiError PROJECT_TASK_UNFINISHED_FORBIDDEN = new ApiError("PROJECT_TASK_UNFINISHED_FORBIDDEN", 4534, "操作失败，有未完成的任务");
    public static final ApiError PROJECT_TEMPLATE_NOT_FOUND = new ApiError("PROJECT_TEMPLATE_NOT_FOUND", 4535, "模板不存在");
    public static final ApiError PROJECT_NOTICE_NODE_IN_USE = new ApiError("PROJECT_NOTICE_NODE_IN_USE", 4536, "通知节点已使用");
    public static final ApiError PROJECT_NOTICE_NODE_NOT_FOUND = new ApiError("PROJECT_NOTICE_NODE_NOT_FOUND", 4537, "通知节点不存在");
    public static final ApiError PROJECT_MEMBER_OR_OTHER_REQUIRED = new ApiError("PROJECT_MEMBER_OR_OTHER_REQUIRED", 4538, "项目人员/其他人员必须填写一个");
    public static final ApiError PROJECT_TEMPLATE_TASK_EXISTS = new ApiError("PROJECT_TEMPLATE_TASK_EXISTS", 4539, "该模板下已存在相同任务名称，不可重复提交");
    public static final ApiError PROJECT_TEMPLATE_TASK_NOT_FOUND = new ApiError("PROJECT_TEMPLATE_TASK_NOT_FOUND", 4540, "模板任务不存在");
    public static final ApiError PROJECT_TEMPLATE_ROLE_EXISTS = new ApiError("PROJECT_TEMPLATE_ROLE_EXISTS", 4541, "模板下已存在该角色");
    public static final ApiError PROJECT_TEMPLATE_MEMBER_REQUIRED = new ApiError("PROJECT_TEMPLATE_MEMBER_REQUIRED", 4542, "请选择模板成员");
    public static final ApiError PROJECT_TEMPLATE_ROLE_MEMBER_EXISTS = new ApiError("PROJECT_TEMPLATE_ROLE_MEMBER_EXISTS", 4543, "模板角色下已存在该成员");
    public static final ApiError PROJECT_TASK_NAME_TOO_LONG = new ApiError("PROJECT_TASK_NAME_TOO_LONG", 4544, "任务名不能超过{0}字符");
    public static final ApiError PROJECT_TASK_RESTART_REQUIRED = new ApiError("PROJECT_TASK_RESTART_REQUIRED", 4545, "只有审核不通过的任务,才可操作重新开始");
    public static final ApiError PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED = new ApiError("PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED", 4546, "任务视图导出类型必填");
    public static final ApiError PROJECT_TASK_SAVE_FORBIDDEN_ARCHIVED = new ApiError("PROJECT_TASK_SAVE_FORBIDDEN_ARCHIVED", 4547, "保存任务失败,该产品已归档或该项目已归档");
    public static final ApiError PROJECT_TASK_COMPLETE_FORBIDDEN_PRODUCT_INCOMPLETE = new ApiError("PROJECT_TASK_COMPLETE_FORBIDDEN_PRODUCT_INCOMPLETE", 4548, "完成任务失败,产品信息尚未填写");
    public static final ApiError PROJECT_PARAM_TASK_OWNER_REQUIRED = new ApiError("PROJECT_PARAM_TASK_OWNER_REQUIRED", 4549, "任务负责人不能为空");
    public static final ApiError PROJECT_FREEZE_REQUIRED = new ApiError("PROJECT_FREEZE_REQUIRED", 4550, "只有归档才能冻结");
    public static final ApiError PROJECT_UNFREEZE_REQUIRED = new ApiError("PROJECT_UNFREEZE_REQUIRED", 4551, "只有已冻结才能解冻");
    public static final ApiError PROJECT_SCRAP_REQUIRED = new ApiError("PROJECT_SCRAP_REQUIRED", 4552, "只有已冻结或者已归档才能报废");
    public static final ApiError PROJECT_RESTORE_REQUIRED = new ApiError("PROJECT_RESTORE_REQUIRED", 4553, "只有已报废才能恢复");
    public static final ApiError PROJECT_CHANGE_REQUEST_REQUIRED = new ApiError("PROJECT_CHANGE_REQUEST_REQUIRED", 4554, "只有归档才能申请变更");
    public static final ApiError PROJECT_CHANGE_VOID_REQUIRED = new ApiError("PROJECT_CHANGE_VOID_REQUIRED", 4555, "变更状态为待审核/审核不通过时,才可以作废");
    public static final ApiError PROJECT_UNARCHIVE_REQUIRED = new ApiError("PROJECT_UNARCHIVE_REQUIRED", 4556, "只有归档才能解除归档");
    public static final ApiError PROJECT_TASK_UNPUBLISH_STATUS_INVALID = new ApiError("PROJECT_TASK_UNPUBLISH_STATUS_INVALID", 4557, "只有待开始，待审核任务才可操作取消发布");
    public static final ApiError PROJECT_TASK_FIXED_RENAME_FORBIDDEN = new ApiError("PROJECT_TASK_FIXED_RENAME_FORBIDDEN", 4558, "固定任务不能修改任务名称");
    public static final ApiError PROJECT_TASK_FIXED_DOC_FORBIDDEN = new ApiError("PROJECT_TASK_FIXED_DOC_FORBIDDEN", 4559, "固定任务不能修改目标交付文档");
    public static final ApiError PROJECT_TASK_FIXED_APPROVAL_FORBIDDEN = new ApiError("PROJECT_TASK_FIXED_APPROVAL_FORBIDDEN", 4560, "固定任务不能修改审核流程");
    public static final ApiError PROJECT_CHANGE_IN_PROGRESS_FORBIDDEN = new ApiError("PROJECT_CHANGE_IN_PROGRESS_FORBIDDEN", 4561, "变更中不能操作");
    public static final ApiError PROJECT_SCHEDULE_TIME_NOT_FOUND = new ApiError("PROJECT_SCHEDULE_TIME_NOT_FOUND", 4562, "排期时间不存在");
    public static final ApiError PROJECT_SCHEDULE_SUBMIT_REQUIRED = new ApiError("PROJECT_SCHEDULE_SUBMIT_REQUIRED", 4563, "只有排期状态为待提交才能提交计划");
    public static final ApiError PROJECT_PRODUCT_SUBMIT_REQUIRED = new ApiError("PROJECT_PRODUCT_SUBMIT_REQUIRED", 4564, "待提交、审核不通过产品信息才能提交");
    public static final ApiError PROJECT_PRODUCT_SCHEDULE_REQUIRED = new ApiError("PROJECT_PRODUCT_SCHEDULE_REQUIRED", 4565, "产品排期不能为空");
    public static final ApiError PROJECT_K3_SEND_REQUIRED = new ApiError("PROJECT_K3_SEND_REQUIRED", 4566, "审核通过才能发送金蝶数据");
    public static final ApiError PROJECT_CHANGE_INFO_NOT_FOUND = new ApiError("PROJECT_CHANGE_INFO_NOT_FOUND", 4567, "变更信息不存在");
    public static final ApiError PROJECT_ROLE_REF_DELETE_FORBIDDEN = new ApiError("PROJECT_ROLE_REF_DELETE_FORBIDDEN", 4568, "角色已被任务引用不支持删除");
    public static final ApiError PROJECT_TEMPLATE_EMPTY = new ApiError("PROJECT_TEMPLATE_EMPTY", 4569, "模板数据为空");
    public static final ApiError PROJECT_PLAN_NOT_FOUND = new ApiError("PROJECT_PLAN_NOT_FOUND", 4570, "产品规划不存在");
    public static final ApiError PROJECT_PLAN_EDIT_REQUIRED = new ApiError("PROJECT_PLAN_EDIT_REQUIRED", 4571, "只有{0}可编辑计划开始-结束时间");
    public static final ApiError PROJECT_PLAN_LINKED_PRODUCT_EXISTS = new ApiError("PROJECT_PLAN_LINKED_PRODUCT_EXISTS", 4572, "产品规划已存在关联产品");
    public static final ApiError PROJECT_PLAN_SYNC_FAILED = new ApiError("PROJECT_PLAN_SYNC_FAILED", 4573, "产品规划同步产品数据失败");
    public static final ApiError PROJECT_PRODUCT_ALREADY_IN_PLAN = new ApiError("PROJECT_PRODUCT_ALREADY_IN_PLAN", 4574, "录入产品已关联规划");
    public static final ApiError PROJECT_SUBTASK_SCHEDULE_FORBIDDEN = new ApiError("PROJECT_SUBTASK_SCHEDULE_FORBIDDEN", 4575, "子任务不能排期变更");
    public static final ApiError PROJECT_FIRST_RECORD_START_TIME_REQUIRED = new ApiError("PROJECT_FIRST_RECORD_START_TIME_REQUIRED", 4576, "第一条数据必须存在开始时间");
    public static final ApiError PROJECT_TASK_DATA_NOT_FOUND = new ApiError("PROJECT_TASK_DATA_NOT_FOUND", 4577, "项目任务数据不存在");
    public static final ApiError PROJECT_TASK_RECORD_NOT_FOUND = new ApiError("PROJECT_TASK_RECORD_NOT_FOUND", 4578, "项目任务记录不存在");
    public static final ApiError PROJECT_SCHEDULE_UNDER_REVIEW_FORBIDDEN = new ApiError("PROJECT_SCHEDULE_UNDER_REVIEW_FORBIDDEN", 4579, "排期还在审核中,无法操作");
    public static final ApiError PROJECT_TASK_APPROVER_MISSING = new ApiError("PROJECT_TASK_APPROVER_MISSING", 4580, "存在无审核人的任务");
    public static final ApiError PROJECT_PLAN_DELETE_FORBIDDEN = new ApiError("PROJECT_PLAN_DELETE_FORBIDDEN", 4581, "规划已关联产品，不支持删除");
    public static final ApiError PROJECT_TASK_DUPLICATE = new ApiError("PROJECT_TASK_DUPLICATE", 4582, "存在重复的任务");
    public static final ApiError PROJECT_TEMPLATE_PREREQUISITE_UPDATE_FAILED = new ApiError("PROJECT_TEMPLATE_PREREQUISITE_UPDATE_FAILED", 4583, "模板前置数据修改失败");
    public static final ApiError PROJECT_TEMPLATE_REQUIRED = new ApiError("PROJECT_TEMPLATE_REQUIRED", 4584, "请选择模板");
    public static final ApiError PROJECT_TASK_AUDIT_STATUS_INVALID = new ApiError("PROJECT_TASK_AUDIT_STATUS_INVALID", 4585, "任务不存在或已审核");
    public static final ApiError PROJECT_PROJECT_RESUME_REQUIRED = new ApiError("PROJECT_PROJECT_RESUME_REQUIRED", 4586, "只有暂停的项目才能重新启动");
    public static final ApiError PROJECT_INITIATE_FORBIDDEN_TERMINATED = new ApiError("PROJECT_INITIATE_FORBIDDEN_TERMINATED", 4587, "已终止,暂停的项目不能进行立项");
    public static final ApiError PROJECT_PAUSE_INVALID = new ApiError("PROJECT_PAUSE_INVALID", 4588, "已完成,终止,暂停的项目不能进行暂停");
    public static final ApiError PROJECT_PAUSE_FORBIDDEN_INIT = new ApiError("PROJECT_PAUSE_FORBIDDEN_INIT", 4589, "已立项,终止,暂停的项目不能进行暂停");
    public static final ApiError PROJECT_CLOSE_REQUIRED = new ApiError("PROJECT_CLOSE_REQUIRED", 4590, "只有已启动,进行中的项目才能结项");
    public static final ApiError PROJECT_INITIATE_FORBIDDEN = new ApiError("PROJECT_INITIATE_FORBIDDEN", 4591, "已立项,终止,暂停的项目不能进行立项");
    public static final ApiError PROJECT_START_REQUIRED = new ApiError("PROJECT_START_REQUIRED", 4592, "只有未启动的项目才能启动项目");
    public static final ApiError PROJECT_TERMINATED_CHANGE_FORBIDDEN = new ApiError("PROJECT_TERMINATED_CHANGE_FORBIDDEN", 4593, "已终止的项目禁止状态更改");
    public static final ApiError PROJECT_TERMINATED_AGAIN_FORBIDDEN = new ApiError("PROJECT_TERMINATED_AGAIN_FORBIDDEN", 4594, "已终止项目不能再次终止");
    public static final ApiError PROJECT_RESUME_ONLY = new ApiError("PROJECT_RESUME_ONLY", 4595, "已暂停的项目只能进行重新启动");
    public static final ApiError PROJECT_INITIATION_COST_REQUIRED = new ApiError("PROJECT_INITIATION_COST_REQUIRED", 4596, "预计立项成本必填，请填写预计立项成本");
    public static final ApiError PROJECT_COST_REQUIRED = new ApiError("PROJECT_COST_REQUIRED", 4597, "预计项目成本必填，请填写预计项目成本");

    static ApiError[] values() {
        return new ApiError[]{
                BOM_REQUIRED,
                BOM_CHANGING,
                BOM_NOT_FOUND,
                BOM_CHILD_NOT_FOUND,
                BOM_CHILD_NOT_FOUND_PARENT,
                BOM_CONTAIN,
                BOM_PARENT_SKU_REPEAT,
                BOM_SKU_REPEAT,
                BOM_COMB_SKU_EXISTS,
                BOM_COMB_STATE_INVALID,
                BOM_COMB_NAME_MISMATCH,
                BOM_COMB_EXPORT_FORBIDDEN,
                BOM_COMB_NOT_FOUND,
                BOM_COMB_SKU_NOT_CHINESE,
                BOM_COMB_SKU_UNAPPROVED,
                BOM_COMB_CHILD_REPEAT,
                BOM_FOR_MABANG_EXIST,
                MOULD_NOT_EXIST,
                MOULD_FILE_AUDITED_ONLY,
                MOULD_REF_SKU_EXISTS,
                MOULD_NOTICE_PURCHASE_ONLY,
                MOULD_NOTICE_NOT_FOUND,
                MOULD_NOTICE_DETAIL_NOT_FOUND,
                MOULD_NOTICE_NO_PUSHABLE,
                MOULD_NOTICE_QTY_EXCEED,
                MOULD_NOT_APPROVED,
                MOULD_LIFESPAN_TOO_SMALL,
                MOULD_RETURN_EXISTS,
                MOULD_ALERT_EXISTS,
                MOULD_COST_REQUIRED,
                MOULD_NOTICE_ALREADY_PUSHED,
                MOULD_PURCHASE_ALREADY_CHANGED,
                MOULD_PURCHASE_ORDER_NOT_FOUND,
                MOULD_PURCHASE_DETAIL_NOT_FOUND,
                MOULD_PURCHASE_NO_PUSHABLE,
                MOULD_PURCHASE_NOT_AUDITED_CHANGE_FORBIDDEN,
                MOULD_PURCHASE_PUSHED_NO_NEW_DETAIL,
                MOULD_PURCHASE_DETAIL_MUST_BE_SAME_ORDER,
                MOULD_PURCHASE_SUPPLIER_INFO_MISSING,
                MOULD_PURCHASE_CHANGE_DETAIL_NOT_FOUND,
                MOULD_PURCHASE_AUDITED_ONLY_FOR_ACCEPTANCE,
                MOULD_CODE_ACCEPT_QTY_EXCEED,
                MOULD_RETURN_EXIST,
                MOULD_ALERT_EXIST,
                PRODUCT_CATEGORY_EXISTS,
                PRODUCT_CATEGORY_HAS_CHILD,
                PRODUCT_CATEGORY_HAS_PRODUCT,
                PRODUCT_NAME_EXISTS,
                PRODUCT_NAME_MISMATCH,
                PRODUCT_NOT_FOUND,
                PRODUCT_SKU_EXISTS,
                PRODUCT_SPU_EXISTS,
                PRODUCT_CATEGORY_NOT_FOUND,
                PRODUCT_SKU_NOT_GENERATED,
                PRODUCT_CATEGORY_CODE_REQUIRED,
                PRODUCT_CATEGORY_CODE_EXISTS,
                PRODUCT_CATEGORY_CODE_RANGE_INVALID,
                PRODUCT_CATEGORY_CODE_NOT_FOUND,
                PRODUCT_CATEGORY_CODE_NOT_ALLOWED,
                PRODUCT_VARIANT_COLOR_NOT_FOUND,
                PRODUCT_VARIANT_INFO_EMPTY,
                PRODUCT_VARIANT_COLOR_EMPTY,
                PRODUCT_VARIANT_COLOR_ATTR_EMPTY,
                PRODUCT_VARIANT_NAME_EXISTS,
                PRODUCT_VARIANT_VALUE_DUPLICATE,
                PRODUCT_VARIANT_COLOR_CODE_DUPLICATE,
                PRODUCT_VARIANT_TYPE_REF_DELETE_FORBIDDEN,
                PRODUCT_VARIANT_VALUES_REF_DELETE_FORBIDDEN,
                PRODUCT_VARIANT_VALUE_NOT_FOUND,
                PRODUCT_INFO_NOT_FOUND,
                PRODUCT_ALREADY_INITIATED,
                PRODUCT_INITIATE_REQUIRED,
                PRODUCT_INITIATE_MISSING_EXISTS,
                PRODUCT_BU_IS_EXISTS_REF,
                PRODUCT_SKU_REQUIRED,
                PRODUCT_SKU_NOT_FOUND,
                PRODUCT_NOT_FOUND_SKU,
                PRODUCT_SKU_RECORD_NOT_FOUND,
                PRODUCT_SKU_APPROVED_REQUIRED,
                PRODUCT_SKU_CHILD_APPROVED_REQUIRED,
                PRODUCT_SKU_EAN_DUPLICATE,
                PRODUCT_SKU_CODE_REQUIRED,
                PRODUCT_SKU_REQUIRED_FOR_OPERATION,
                PRODUCT_SKU_IN_USE_DELETE_FORBIDDEN,
                PRODUCT_SKU_STOCK_REF_CHANGE_FORBIDDEN,
                PRODUCT_EXIST_SKU,
                PRODUCT_REQUIRED_FIELDS_INCOMPLETE,
                PRODUCT_PM_REQUIRED,
                PRODUCT_SALES_METHOD_REQUIRED,
                PRODUCT_CATEGORY_REQUIRED,
                PRODUCT_BRAND_REQUIRED,
                PRODUCT_LEVEL_REQUIRED,
                PRODUCT_STYLE_NAME_CN_REQUIRED,
                PRODUCT_STYLE_NAME_EN_REQUIRED,
                PRODUCT_ATTR_REQUIRED,
                PRODUCT_SALES_CHANNEL_REQUIRED,
                PRODUCT_COMMISSIONED_DEV_COST_REQUIRED,
                PRODUCT_TARGET_COST_REQUIRED,
                PRODUCT_TARGET_COST_EX_TAX_REQUIRED,
                PRODUCT_RETAIL_PRICE_REQUIRED,
                PRODUCT_MASS_PRODUCTION_COST_REQUIRED,
                PRODUCT_TAX_RATE_REQUIRED,
                PRODUCT_ANNUAL_SALES_REQUIRED,
                PRODUCT_MONTHLY_SALES_REQUIRED,
                PRODUCT_Q1_SALES_REQUIRED,
                PRODUCT_ANNUAL_SALES_AMOUNT_REQUIRED,
                PRODUCT_MONTHLY_SALES_AMOUNT_REQUIRED,
                PRODUCT_SALES_COUNTRY_REQUIRED,
                PRODUCT_IMAGE_COMPLETE_REQUIRED,
                PRODUCT_VIDEO_COMPLETE_REQUIRED,
                PRODUCT_SALES_STATUS_REQUIRED,
                PRODUCT_SALEABLE_FLAG_REQUIRED,
                PRODUCT_SALES_PLATFORM_REQUIRED,
                PRODUCT_COST_INFO_REQUIRED,
                PRODUCT_SALES_INFO_REQUIRED,
                PRODUCT_PACK_REQUIRED,
                PRODUCT_SIZE_REQUIRED,
                PRODUCT_BOX_SIZE_REQUIRED,
                PRODUCT_GROSS_WEIGHT_REQUIRED,
                PRODUCT_BOX_WEIGHT_REQUIRED,
                PRODUCT_NET_WEIGHT_REQUIRED,
                PRODUCT_BOX_QTY_REQUIRED,
                PRODUCT_ITERATE_SKU_REQUIRED,
                PRODUCT_CERTIFICATE_EXISTS,
                PRODUCT_BASIC_LABEL_EXISTS,
                PRODUCT_BASIC_LABEL_LEVEL_NOT_FOUND,
                PRODUCT_BASIC_LABEL_NAME_EXISTS,
                PRODUCT_BASIC_LABEL_SAVE_FAILED,
                PRODUCT_BASIC_LABEL_REL_SAVE_FAILED,
                PRODUCT_INFO_CHANGE_CONTENT_REQUIRED,
                PRODUCT_TRIAL_DETAIL_NOT_FOUND,
                PRODUCT_VENDOR_PRICE_FETCH_FAILED,
                PRODUCT_VENDOR_PRICE_NOT_SUBMITTED,
                PRODUCT_VENDOR_PRICE_NOT_FOUND,
                PRODUCT_UPLOAD_FORBIDDEN_IN_APPROVING,
                PRODUCT_CREATE_FAILED,
                PRODUCT_SKU_NOT_APPROVED_REVOKE_APPROVAL_FORBIDDEN,
                PRODUCT_ATTR_IN_USE_DELETE_FORBIDDEN,
                PRODUCT_SKU_FIN_CODE_NOT_FOUND,
                PRODUCT_SKU_NOT_COST,
                PRODUCT_SKU_MABANG_FIN_CODE_NOT_FOUND,
                PRODUCT_SKU_DUPLICATE,
                PRODUCT_SKU_OCCUPY_STATE_UPDATE_FAIL,
                PRODUCT_SKU_STOCK_EXISTS_SPU_CHANGE_FORBIDDEN,
                PRODUCT_PACKING_SKU_IS_NOT_NULL,
                PRODUCT_PACKING_SKU_PACK_QTY_IS_NOT_NULL,
                PRODUCT_PACKING_SKU_BOX_QTY_IS_NOT_NULL,
                PRODUCT_SKU_PARAM_NOT_FOUND,
                PRODUCT_DEV_STATUS_REQUIRED,
                PRODUCT_APP_CATEGORY_CODE_EXISTS,
                PRODUCT_APP_CATEGORY_NAME_EXISTS,
                PRODUCT_PROPERTY_ASSET_NOT_EXIST,
                PRODUCT_SALES_BATTERY_WEIGHT_NOT_NULL,
                PRODUCT_IMG_ATTACHMENT_SAVE_FAILED,
                PRODUCT_IMG_ATTACHMENT_NOT_FOUND,
                PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_ALL,
                PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_MAIN,
                PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_THUMBNAIL,
                PRODUCT_IMG_DOWNLOAD_MIN_REQUIRED,
                PRODUCT_IMG_DOWNLOAD_MAX_LIMIT,
                PRODUCT_IMG_DOWNLOAD_NOT_FOUND,
                PRODUCT_CHANGE_SKU_NOT_APPROVE,
                PRODUCT_CHANGE_PRODUCT_SIZE_CHANGE,
                PRODUCT_CHANGE_BOX_SIZE_CHANGE,
                PRODUCT_CHANGE_EXIST,
                PRODUCT_RETAIL_PRICE_MISSING,
                PRODUCT_RETAIL_PRICE_MISSING_ZERO,
                PRODUCT_RETAIL_SKU_MISSING,
                PRODUCT_RETAIL_SKU_DUPLICATE,
                PROJECT_TASK_REQUIRED,
                PROJECT_TASK_NAME_REQUIRED,
                PROJECT_TASK_OWNER_REQUIRED,
                PROJECT_OWNER_NOT_FOUND,
                PROJECT_PM_REQUIRED,
                PROJECT_RND_CENTER_OWNER_REQUIRED,
                PROJECT_PMO_OWNER_REQUIRED,
                PROJECT_DATE_START_AFTER_END,
                PROJECT_DATE_END_AFTER_START,
                PROJECT_STAGE_TASK_EXISTS,
                PROJECT_STAGE_TASK_REQUIRED,
                PROJECT_STAGE_INIT_NAME_IMMUTABLE,
                PROJECT_TEMPLATE_EXISTS,
                PROJECT_TASK_EXISTS,
                PROJECT_ROLE_EXISTS,
                PROJECT_ARCHIVE_FORBIDDEN_NOT_DONE,
                PROJECT_STAGE_INIT_DELETE_FORBIDDEN,
                PROJECT_FIELD_EXISTS,
                PROJECT_TASK_HAS_CHILD,
                PROJECT_NOT_FOUND,
                PROJECT_TASK_NOT_FOUND,
                PROJECT_TASK_BATCH_STATUS_FORBIDDEN,
                PROJECT_TASK_PUBLISH_REQUIRED,
                PROJECT_TASK_START_REQUIRED,
                PROJECT_TASK_CLOSE_REQUIRED,
                PROJECT_TASK_REVIEW_COMPLETE_FORBIDDEN,
                PROJECT_TASK_PREDECESSOR_UNFINISHED,
                PROJECT_TASK_CHILD_UNFINISHED,
                PROJECT_TASK_DOC_CHANGE_FORBIDDEN,
                PROJECT_STAGE_TASK_NOT_FOUND,
                PROJECT_STAGE_INITIATION_DELETE_FORBIDDEN,
                PROJECT_STAGE_HAS_TASKS_DELETE_FORBIDDEN,
                PROJECT_TASK_COMPLETE_REQUIRED,
                PROJECT_TASK_DELIVERABLE_UNFINISHED,
                PROJECT_TASK_UNFINISHED_FORBIDDEN,
                PROJECT_TEMPLATE_NOT_FOUND,
                PROJECT_NOTICE_NODE_IN_USE,
                PROJECT_NOTICE_NODE_NOT_FOUND,
                PROJECT_MEMBER_OR_OTHER_REQUIRED,
                PROJECT_TEMPLATE_TASK_EXISTS,
                PROJECT_TEMPLATE_TASK_NOT_FOUND,
                PROJECT_TEMPLATE_ROLE_EXISTS,
                PROJECT_TEMPLATE_MEMBER_REQUIRED,
                PROJECT_TEMPLATE_ROLE_MEMBER_EXISTS,
                PROJECT_TASK_NAME_TOO_LONG,
                PROJECT_TASK_RESTART_REQUIRED,
                PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED,
                PROJECT_TASK_SAVE_FORBIDDEN_ARCHIVED,
                PROJECT_TASK_COMPLETE_FORBIDDEN_PRODUCT_INCOMPLETE,
                PROJECT_PARAM_TASK_OWNER_REQUIRED,
                PROJECT_FREEZE_REQUIRED,
                PROJECT_UNFREEZE_REQUIRED,
                PROJECT_SCRAP_REQUIRED,
                PROJECT_RESTORE_REQUIRED,
                PROJECT_CHANGE_REQUEST_REQUIRED,
                PROJECT_CHANGE_VOID_REQUIRED,
                PROJECT_UNARCHIVE_REQUIRED,
                PROJECT_TASK_UNPUBLISH_STATUS_INVALID,
                PROJECT_TASK_FIXED_RENAME_FORBIDDEN,
                PROJECT_TASK_FIXED_DOC_FORBIDDEN,
                PROJECT_TASK_FIXED_APPROVAL_FORBIDDEN,
                PROJECT_CHANGE_IN_PROGRESS_FORBIDDEN,
                PROJECT_SCHEDULE_TIME_NOT_FOUND,
                PROJECT_SCHEDULE_SUBMIT_REQUIRED,
                PROJECT_PRODUCT_SUBMIT_REQUIRED,
                PROJECT_PRODUCT_SCHEDULE_REQUIRED,
                PROJECT_K3_SEND_REQUIRED,
                PROJECT_CHANGE_INFO_NOT_FOUND,
                PROJECT_ROLE_REF_DELETE_FORBIDDEN,
                PROJECT_TEMPLATE_EMPTY,
                PROJECT_PLAN_NOT_FOUND,
                PROJECT_PLAN_EDIT_REQUIRED,
                PROJECT_PLAN_LINKED_PRODUCT_EXISTS,
                PROJECT_PLAN_SYNC_FAILED,
                PROJECT_PRODUCT_ALREADY_IN_PLAN,
                PROJECT_SUBTASK_SCHEDULE_FORBIDDEN,
                PROJECT_FIRST_RECORD_START_TIME_REQUIRED,
                PROJECT_TASK_DATA_NOT_FOUND,
                PROJECT_TASK_RECORD_NOT_FOUND,
                PROJECT_SCHEDULE_UNDER_REVIEW_FORBIDDEN,
                PROJECT_TASK_APPROVER_MISSING,
                PROJECT_PLAN_DELETE_FORBIDDEN,
                PROJECT_TASK_DUPLICATE,
                PROJECT_TEMPLATE_PREREQUISITE_UPDATE_FAILED,
                PROJECT_TEMPLATE_REQUIRED,
                PROJECT_TASK_AUDIT_STATUS_INVALID,
                PROJECT_PROJECT_RESUME_REQUIRED,
                PROJECT_INITIATE_FORBIDDEN_TERMINATED,
                PROJECT_PAUSE_INVALID,
                PROJECT_PAUSE_FORBIDDEN_INIT,
                PROJECT_CLOSE_REQUIRED,
                PROJECT_INITIATE_FORBIDDEN,
                PROJECT_START_REQUIRED,
                PROJECT_TERMINATED_CHANGE_FORBIDDEN,
                PROJECT_TERMINATED_AGAIN_FORBIDDEN,
                PROJECT_RESUME_ONLY,
                PROJECT_INITIATION_COST_REQUIRED,
                PROJECT_COST_REQUIRED,
        };
    }
}
