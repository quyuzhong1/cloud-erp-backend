package com.erp.server.dmp.entity.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class KingdeeDeliveryDetailItemEntity {
    private String fBillNo;
    private String fSoorDerno;
    private String fSrcBillNo;
    private String f_ulz_BaseProperty1;
    private String fCustMatID;
    private String fCustMatName;
    private String fMaterialID;
    private String fMaterialNumber;
    private String fMaterialName;
    private String fBarcode;
    private String fMateriaModel;
    private String fMateriaType;
    private String fRealQty;
    private String fUnitName;
    private String fPrice;
    private String fIsFree;
    private String fArrivalStatus;
    private String fArrivalDate;
    private String fAmount;
    private String fStockStatusID;
    private String fStockStatusName;
    private String fStockName;
    private String f_ulz_Text1;
    private String fEntryCostAmount;
    private String fEntrynote;
}
