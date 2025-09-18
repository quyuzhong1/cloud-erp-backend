package com.erp.sdk.third.kingdee.utils;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/3 11:46
 */
public enum KingdeePushModuleEnum {

    BD_MATERIAL("BD_MATERIAL","物料"),
    ENG_BOM("ENG_BOM","物料清单"),
    STK_TRANSFER_DIRECT("STK_TransferDirect","直接调拨单"),
    PLM_CFG_PREFERRED_ORGANIZATION_CFG("PLM_CFG_PreferredOrganizationCFG","下推首选组织配置"),
    BOS_ASSISTANTDATA_DETAIL("BOS_ASSISTANTDATA_DETAIL","辅助资料列表"),
    BD_EMPINFO("BD_Empinfo","员工"),
    PUR_PURCHASEORDER("PUR_PurchaseOrder","采购订单"),
    PUR_PRICECATEGORY("PUR_PriceCategory","采购价目表"),
    PUR_MRB("PUR_MRB","采购退料单"),
    PUR_PAT("PUR_PAT","采购调价表"),
    BD_STOCK("BD_STOCK","仓库"),
    BD_SUPPLIER("BD_Supplier","供应商"),
    STK_INSTOCK("STK_InStock","采购入库单"),
    BD_DEPARTMENT("BD_Department","部门"),
    STK_TRANSFERDIRECT("STK_TransferDirect","直接调拨单"),
    BD_CUSTOMER("BD_Customer","客户"),
    STK_MISDELIVERY("STK_MisDelivery","其他出库单"),
    STK_MISCELLANEOUS("STK_MISCELLANEOUS","其他入库单"),
    STK_ASSEMBLEDAPP("STK_AssembledApp","组装拆卸"),
    SAL_SALEORDER("SAL_SaleOrder","销售订单"),
    SAL_SALEORDER_CHANGE("SAL_XORDER","销售订单变更"),
   // SAVE_X_SALE_ORDER("SaveXSaleOrder","销售订单新变更单"),
    SAL_OUTSTOCK("SAL_OUTSTOCK","销售出库单"),
    SAL_RETURNSTOCK("SAL_RETURNSTOCK","销售退货单"),
    BD_COMMONCONTACT("BD_CommonContact","联系人"),
    SUB_SUBREQORDER("SUB_SUBREQORDER","委外订单"),
    SUB_REQCHANGE("SUB_ReqChange","委外变更单"),
    STK_STOCKCOUNTGAIN("STK_StockCountGain","盘盈单"),
    STK_STOCKCOUNTLOSS("STK_StockCountLoss","盘亏单"),
    PUR_POXCHANGE("PUR_POXChange","采购变更"),
    PUR_RECEIVEBILL("PUR_ReceiveBill","收料通知单"),
    SUB_PICKMTRL("SUB_PickMtrl","委外领料单"),
    CN_BANKACNT("CN_BANKACNT","银行账号"),
    BD_RECCONDITION("BD_RecCondition","收款条件"),
    BD_PAYMENTCONDITION("BD_PaymentCondition","付款条件"),
    HR_ORG_HRPOST("HR_ORG_HRPOST","岗位信息"),
    BD_NEWSTAFF("BD_NEWSTAFF","员工任岗位信息"),
    BD_OPERATOR("BD_OPERATOR","业务员信息"),
    STK_TRANSFERIN("STK_TRANSFERIN","分步式调入单"),
    STK_TRANSFEROUT("STK_TRANSFEROUT","分步式调出单"),
    AP_PAYABLE("AP_Payable","应付单"),



    ;
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    KingdeePushModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
