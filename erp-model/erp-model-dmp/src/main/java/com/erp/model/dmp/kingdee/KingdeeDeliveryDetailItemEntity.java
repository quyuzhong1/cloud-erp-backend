package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class KingdeeDeliveryDetailItemEntity {
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
    @Alias("FEntryCostAmount")
    private String fEntryCostAmount;
    @Alias("FEntrynote")
    private String fEntryNote;
}
