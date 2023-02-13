package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class KingdeeReturnOrderItemEntity {
    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FOrderNo")
    private String fOrderNo;
    @Alias("FAmount")
    private String fAmount;
    @Alias("FMustqty")
    private String fMustQty;
    @Alias("FUnitID.FName")
    private String fUnitName;
    @Alias("FMaterialId")
    private String fMaterialId;
    @Alias("FMaterialId.FNumber")
    private String fMaterialNumber;
    @Alias("FMaterialName")
    private String fMaterialName;
    @Alias("FAuxpropId")
    private String fAuxPropId;
    @Alias("FMaterialType")
    private String fMaterialType;
    @Alias("FPrice")
    private String fPrice;
    @Alias("FStockId")
    private String fStockId;
    @Alias("FStocklocId")
    private String fStockLocId;
    @Alias("FStockstatusId")
    private String fStockStatusId;
    @Alias("FNote")
    private String fNote;
    @Alias("FSrcBillNo")
    private String fSrcBillNo;
    @Alias("FSrcBillTypeID")
    private String fSrcBillTypeID;
    @Alias("FIsFree")
    private String fIsFree;
    @Alias("FMaterialModel")
    private String fMaterialModel;
    @Alias("FRealQty")
    private String fRealQty;
    @Alias("FSOBILLTYPEID")
    private String fSoBillTypeId;
    @Alias("FSalUnitQty")
    private String fSalUnitQty;
    @Alias("FProjectNo")
    private String fProjectNo;
    @Alias("F_ulz_KHSKU")
    private String f_ulz_KHSKU;

    /**
     * 价税合计
     */
    @Alias("FAllAmount")
    private BigDecimal fAllAmount;
}
