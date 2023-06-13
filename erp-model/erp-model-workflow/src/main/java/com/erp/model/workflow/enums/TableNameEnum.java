package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TableNameEnum {
    //plm
    PRODUCT_BOM_INFO("product_bom_info", "BOM管理"),
    PRODUCT_DETAIL("product_detail", "产品管理"),
    PROJECT_TASK("project_task", "任务列表"),
    PRODUCT_CHANGE("product_change", "变更管理"),
    //scm
    PURCHASE_PRICE_CHANGE("purchase_price_change", "采购调价表"),
    SALES_DEMAND("sales_demand", "备货申请单"),
    PURCHASE_APPLICATION("purchase_application", "采购申请单"),
    PURCHASE_ORDER("purchase_order", "采购订单"),
    PURCHASE_CHANGE("purchase_change", "采购变更单"),
    PURCHASE_PRICE("purchase_price", "采购价目表"),
    //wms
    QC_INFO("qc_info", "质检单"),
    PO_RECEIVE("po_receive", "收货单"),
    PO_INSTOCK("po_instock", "入库单"),
    PO_RETURN("po_return", "采购退货单"),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    TableNameEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (TableNameEnum state : TableNameEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static TableNameEnum getByCode(String code) {
        return Arrays.stream(TableNameEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
