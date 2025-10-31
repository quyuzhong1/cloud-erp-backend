package com.common.message.enums;

/**
 * @author Will
 * @version 1.0
 * @description: API模块类型枚举(sys字典表apiModuleType)
 * @date 2023/1/11 14:54
 */
public enum ApiModuleTypeEnum {

    PRODUCT_DETAIL(0, "productDetail", "产品信息","plm"),
    BOM_INFO(1, "bomManage", "BOM管理","plm"),
    STOCK_OVERSEAS(2, "stockOverseas", "海外仓库存",""),
    ASSISTANT_DATA(3, "assistantData", "辅助资料",""),
    ONE_LEVEL_CATEGORY(4, "oneLevelCategory", "一级分类","plm"),
    APPLICATION_CATEGORY(99, "applicationCategory", "应用分类","plm"),
    SECOND_LEVEL_CATEGORY(5, "secondLevelCategory", "二级分类","plm"),
    SYS_USER_INFO(6, "sysUserInfo", "员工","sys"),
    CHANGE_ORG(7, "changeOrg", "默认组织切换",""),
    PURCHASE_ORDER(8, "purchaseOrder", "采购订单","scm"),
    PURCHASE_PRICE(9, "PurchasePrice", "采购价目表","scm"),
    PURCHASE_RETURN_ORDER(10, "purchaseReturnOrder", "采购退货单","wms"),
    PURCHASE_PRICE_CHANGE(11, "PurchasePriceChange", "采购调价表","scm"),
    WAREHOUSE_INFO(12, "warehouseManage", "仓库管理","wms"),
    SUPPLIER(13, "supplier", "供应商管理","scm"),
    PURCHASE_STOCK_IN(14, "purchaseStockIn", "入库单","wms"),
    SYS_DEPARTMENT(15, "sysDepartment", "部门","sys"),
    TRANSFER_INFO(16, "transferInfo", "直接调拨单","wms"),
    CUSTOMER_INFO(17, "customerInfo", "客户","oms"),
    OTHER_OUTSTOCK(18, "otherOutstock", "其他出库单","wms"),
    OTHER_INSTOCK(19, "otherInstock", "其他入库单","wms"),
    MACHINE_INFO(20, "machineInfo", "加工单","wms"),
    SO_INFO(21, "soInfo", "销售订单","oms"),
    SO_CHANGE(22, "soChange", "销售变更单","oms"),
    SO_OUTSTOCK(23, "soOutstock", "销售出库单","wms"),
    SO_RETURN(24, "soReturn", "销售退货单","wms"),
    CUSTOMER_GROUP(25, "customerGroup", "客户分组","oms"),
    CUSTOMER_CONTACT(26, "customerContact", "客户联系人","oms"),
    SUBCONTRACT_ORDER(27, "subcontractOrder", "委外订单","scm"),
    SUBCONTRACT_CHAGE(28, "subcontractChange", "委外变更单","scm"),
    STOCKTAKING_PROFIT(29, "stocktakingProfit", "盘盈单","wms"),
    STOCKTAKING_LOSS(30, "stocktakingLoss", "盘亏单","wms"),
    PURCHASE_CHANGE(31, "purchaseChange", "采购变更单","scm"),

    PO_RECEIVE(32, "poReceive", "采购收货单","wms"),
    SUBCONTRACT_ISSUE(33, "subcontractIssue", "委外发料单","wms"),
    SYS_POST(34, "post", "金蝶岗位","sys"),
    SYS_USER_POST(35, "userPost", "金蝶员工任岗","sys"),
    KINGDEE_OPERATOR(36, "kingdeeOperator", "金蝶业务员","sys"),
    GLOBAL_AREA(37, "globalArea", "区域","sys"),
    COUNTRY(38, "country", "国家","sys"),
    PROVINCE_CITY(39, "provinceCity", "省市","sys"),
    WDT_PRODUCT(40, "wdtProduct", "旺店通产品资料","plm")
    , WDT_OTHER_IN_STOCK(41, "wdtOtherInStock", "旺店通其他入库单", "wms"),
    WDT_OTHER_OUT_STOCK(42, "wdtOtherOutStock", "旺店通其他出库单", "wms"),
    WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL(43, "wdtVwAllocationHandleDetail", "旺店通分货单处理明细", "wms"),
    WDT_EXT_OUT_STOCK(44, "wdtExtOutStock", "旺店通外仓调整出库单", "wms"),
    WDT_EXT_IN_STOCK(45, "wdtExtInStock", "旺店通外仓调整入库单", "wms"),
    TRANSFER_IN(46, "transferIn", "分步式调入单","wms"),
    TRANSFER_OUT(47, "transferOut", "分步式调出单","wms"),
    PAYABLE_INFO(48, "payableInfo", "采购对账","srm"),

    BD_RATE(49, "bdRate", "汇率","sys"),
    ASSET_PURCHASE_ORDER(50, "assetPurchaseOrder", "资产采购订单","scm"),
    ;
    private Integer code;

    private String name;

    private String desc;

    private String system;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSystem() {
        return system;
    }

    ApiModuleTypeEnum(Integer code, String name, String desc,String system) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.system = system;
    }

    public static ApiModuleTypeEnum getByCode(Integer code) {
        ApiModuleTypeEnum[] values = values();
        for (ApiModuleTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ApiModuleTypeEnum getByName(String name) {
        ApiModuleTypeEnum[] values = values();
        for (ApiModuleTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
