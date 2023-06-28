package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
public enum SourceTypeEnum {
    SELF_ADD("selfAdd", "手动新增",""),
    KINGDEE("kingdee", "金蝶",""),

    //SCM
    PURCHASE_ORDER("purchaseOrder", "采购订单","purchase_order"),
    PURCHASE_APPLICATION("purchaseApplication", "采购申请单","purchase_application"),
    SUBCONTRACT_ORDER("subcontractOrder", "委外订单","subcontract_order"),
    PURCHASE_PRICE_CHANGE("subcontractOrder", "采购价变更","purchase_price_change"),
    SALES_DEMAND("salesDemand", "备货申请","sales_demand"),
    PURCHASE_CHANGE("purchaseChange", "采购变更","purchase_change"),
    PURCHASE_PRICE("purchasePrice", "采购价目表","purchase_price"),



    //WMS
    PO_INSTOCK("poInstock", "采购入库单","po_instock"),
    QC_INFO("qcInfo", "质检单","qc_info"),
    PO_RETURN("poReturn", "采购退货单","po_return"),
    PO_RETURN_DETAIL("poReturnDetail", "采购退货单详情","po_return_detail"),
    PO_RECEIVE("poReceive", "仓库签收单","po_receive"),
    TRANSFER_APPLICATION("transferApplication", "调拨申请单","transfer_application"),
    SO_RETURN_NOTICE("soReturnNotice", "销售退货通知单","so_return_notice"),
    SO_RETURN_RECEIVE("soReturnReceive", "销售退货签收单","so_return_receive"),
    SO_RETURN_INSTOCK("soReturnInstock", "销售退货入库单","so_return_instock"),
    SO_DELIVERY_NOTICE("soDeliveryNotice", "销售发货通知单","so_delivery_notice"),
    TRANSFER_OUT("transferOut", "分布式调出单","transfer_out"),

    //OMS
    SO_RETURN("soReturn", "销售退货订单","so_return"),
    SO_INFO("soInfo", "销售订单","so_info"),
    CUSTOMER_INFO( "customerInfo", "客户表","customer_info"),

    //Kingdee
    SAL_RETURNSTOCK("SAL_RETURNSTOCK", "金蝶销售退货单",""),

    // PLM
    PRODUCT_BOM_INFO("productBomInfo", "BOM管理","product_bom_info"),
    PRODUCT_DETAIL("productDetail", "产品管理","product_detail"),
    PROJECT_TASK("projectTask", "任务列表","project_task"),
    PRODUCT_CHANGE("productChange", "变更管理","product_change"),

    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    private String tableName;

    SourceTypeEnum(String type, String name, String tableName) {
        this.code = type;
        this.name = name;
        this.tableName = tableName;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public static String getName(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (type.equals(sourceTypeEnum.getCode())) {
                return sourceTypeEnum.name();
            }
        }
        return "";
    }

    public static SourceTypeEnum of(String code) {
        return Arrays.stream(SourceTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}
