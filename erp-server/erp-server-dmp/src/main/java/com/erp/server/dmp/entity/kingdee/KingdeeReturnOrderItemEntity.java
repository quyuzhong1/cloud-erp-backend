package com.erp.server.dmp.entity.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class KingdeeReturnOrderItemEntity {
    private String fBillNo;
    private String fOrderNo;
    private String fAmount;
    private String fMustqty;
    private String fUnitName;
    private String fMaterialId;
    private String fMaterialNumber;
    private String fMaterialName;
    private String fAuxpropId;
    private String fMaterialType;
    private String fPrice;
    private String fStockId;
    private String fStocklocId;
    private String fStockstatusId;
    private String fNote;
    private String fSrcBillNo;
    private String fSrcBillTypeID;
    private String fIsFree;
    private String fMaterialModel;
    private String fRealQty;
    private String fSOBILLTYPEID;
    private String fSalUnitQty;
    private String fProjectNo;
    private String f_ulz_KHSKU;
}
