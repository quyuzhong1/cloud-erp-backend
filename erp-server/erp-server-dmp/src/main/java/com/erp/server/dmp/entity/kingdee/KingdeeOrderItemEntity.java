package com.erp.server.dmp.entity.kingdee;

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
    private String fMaterialModel;
    private BigDecimal fQty;
    private String fPriceUnitQty;
    private String fUnitID;
    private String fAuxPropId;
    private BigDecimal fPrice;
    private String fEntryTaxRate;
    private String fTaxPrice;
    private String fIsFree;
    private String fEntryTaxAmount;
    private String fMaterialType;
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
    private BigDecimal f_ulz_Decimal;
    private BigDecimal f_ulz_CGCB;
    private String FSOStockId;
}
