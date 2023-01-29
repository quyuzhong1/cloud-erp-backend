package com.erp.model.dmp.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class KingdeeOrderItemEntity {
    private String fBillNo;
    private String fReturnType;
    private String fRowType;
    private String fMaterialName;
    private String fMaterialGroup;
    private String fMaterialId;
    private String fMaterialNumber;
    private String fMaterialModel;
    private BigDecimal fQty;
    private String fPriceUnitQty;
    private String fUnitID;
    private String fAuxPropId;
    /**
     * 销售单价
     */
    private BigDecimal fPrice;
    /**
     * 税率
     */
    private String fEntryTaxRate;

    /**
     * 含税单价
     */
    private String fTaxPrice;
    private String fIsFree;
    /**
     * 税额
     */
    private String fEntryTaxAmount;
    private String fMaterialType;
    /**
     * 销售金额
     */
    private String fAmount;
    private String fBarcode;
    private String fMapName;
    private String f_ulz_BaseProperty;
    private String fMapId;
    private String fBaseUnitId;
    private Integer fOldQty;
    private String fTaxNetPrice;
    private String fDiscount;
    private String fPriceDiscount;
    private String fBranchId;
    private String fEntryNote;
    private String fSrcType;
    private String fSrcBillNo;
    private String fMinPlanDeliveryDate;
    private String fDeliveryStatus;
    /**
     * 销售总成本
     */
    private BigDecimal f_ulz_Decimal;
    /**
     * 采购单价
     */
    private BigDecimal f_ulz_CGCB;
    private String FSOStockId;

    private BigDecimal FAllAmount;
}
