package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class KingdeeOrderItemEntity {
    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FReturnType")
    private String fReturnType;
    @Alias("FRowType")
    private String fRowType;
    @Alias("FMaterialName")
    private String fMaterialName;
    @Alias("FMaterialGroup")
    private String fMaterialGroup;
    @Alias("FMaterialId")
    private String fMaterialId;
    @Alias("FMaterialId.FNumber")
    private String fMaterialNumber;
    @Alias("FMaterialModel")
    private String fMaterialModel;
    @Alias("FQty")
    private BigDecimal fQty;
    @Alias("FPriceUnitQty")
    private String fPriceUnitQty;
    @Alias("FUnitID")
    private String fUnitID;
    @Alias("FAuxPropId")
    private String fAuxPropId;
    /**
     * 销售单价
     */
    @Alias("FPrice")
    private BigDecimal fPrice;
    /**
     * 税率
     */
    @Alias("FEntryTaxRate")
    private String fEntryTaxRate;

    /**
     * 含税单价
     */
    @Alias("FTaxPrice")
    private String fTaxPrice;
    @Alias("FIsFree")
    private String fIsFree;
    /**
     * 税额
     */
    @Alias("FEntryTaxAmount")
    private String fEntryTaxAmount;
    @Alias("FMaterialType")
    private String fMaterialType;
    /**
     * 销售金额
     */
    @Alias("FAmount")
    private String fAmount;
    @Alias("FBarcode")
    private String fBarcode;
    @Alias("FMapName")
    private String fMapName;
    @Alias("F_ulz_BaseProperty")
    private String f_ulz_BaseProperty;
    @Alias("FMapId")
    private String fMapId;
    @Alias("FBaseUnitId")
    private String fBaseUnitId;
    @Alias("FOldQty")
    private Integer fOldQty;
    @Alias("FTaxNetPrice")
    private String fTaxNetPrice;
    @Alias("FDiscount")
    private String fDiscount;
    @Alias("FPriceDiscount")
    private String fPriceDiscount;
    @Alias("FBranchId")
    private String fBranchId;
    @Alias("FEntryNote")
    private String fEntryNote;
    @Alias("FSrcType")
    private String fSrcType;
    @Alias("FSrcBillNo")
    private String fSrcBillNo;
    @Alias("FMinPlanDeliveryDate")
    private String fMinPlanDeliveryDate;
    @Alias("FDeliveryStatus")
    private String fDeliveryStatus;
    /**
     * 销售总成本
     */
    @Alias("F_ulz_Decimal")
    private BigDecimal f_ulz_Decimal;
    /**
     * 采购单价
     */
    @Alias("F_ulz_CGCB")
    private BigDecimal f_ulz_CGCB;
    @Alias("FSOStockId.FName")
    private String FSOStockId;
    /**
     * 价税合计
     */
    @Alias("FAllAmount")
    private BigDecimal FAllAmount;
}
