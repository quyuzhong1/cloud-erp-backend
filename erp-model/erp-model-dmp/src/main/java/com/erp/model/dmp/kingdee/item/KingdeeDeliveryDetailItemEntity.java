package com.erp.model.dmp.kingdee.item;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class KingdeeDeliveryDetailItemEntity {

    @Alias("FEntity_FENTRYID")
    private String fEntryId;
    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FSoorDerno")
    private String fSoorDerno;
    @Alias("FSrcBillNo")
    private String fSrcBillNo;
    @Alias("F_ulz_BaseProperty1")
    private String f_ulz_BaseProperty1;
    @Alias("FCustMatID")
    private String fCustMatID;
    @Alias("FCustMatName")
    private String fCustMatName;
    @Alias("FMaterialID")
    private String fMaterialID;
    @Alias("FMaterialID.FNumber")
    private String fMaterialNumber;
    @Alias("FMaterialID.FName")
    private String fMaterialName;
    @Alias("FBarcode")
    private String fBarcode;
    @Alias("FMateriaModel")
    private String fMateriaModel;
    @Alias("FMateriaType")
    private String fMateriaType;
    @Alias("FRealQty")
    private String fRealQty;
    @Alias("FUnitID.FName")
    private String fUnitName;
    @Alias("FPrice")
    private String fPrice;

    @Alias("FTaxPrice")
    private BigDecimal fTaxPrice;
    @Alias("FIsFree")
    private String fIsFree;
    @Alias("FArrivalStatus")
    private String fArrivalStatus;
    @Alias("FArrivalDate")
    private String fArrivalDate;
    @Alias("FAmount")
    private String fAmount;
    @Alias("FStockStatusID")
    private String fStockStatusID;
    @Alias("FStockStatusID.FName")
    private String fStockStatusName;
    @Alias("FStockID.FName")
    private String fStockName;
    @Alias("F_ulz_Text1")
    private String f_ulz_Text1;
    @Alias("FEntrynote")
    private String fEntryNote;
    /**
     * 汇率
     */
    @Alias("FExchangeRate")
    private String fExchangeRate;
    /**
     * 金额（本位币）
     */
    @Alias("FAmount_LC")
    private BigDecimal fAmount_LC;

    @Alias("FSrcType")
    private String fSrcType;
    /**
     * 备注
     */
    @Alias("FNote")
    private String fNote;
    /**
     * 价税合计（本位币）
     */
    @Alias("FAllAmount")
    private BigDecimal fAllAmount;
    /**
     * 价税合计（本位币）
     */
    @Alias("FAllAmount_LC")
    private BigDecimal fAllAmount_LC;
    /**
     * 成本价（本位币）
     */
    @Alias("FCostPrice")
    private BigDecimal fCostPrice;
    /**
     * 总成本(本位币)
     */
    @Alias("FCostAmount_LC")
    private BigDecimal fCostAmount_LC;
    /**
     * 销售成本价
     */
    @Alias("FSalCostPrice")
    private BigDecimal fSalCostPrice;
}
