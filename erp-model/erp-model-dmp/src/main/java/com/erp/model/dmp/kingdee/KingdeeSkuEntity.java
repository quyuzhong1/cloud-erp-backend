package com.erp.model.dmp.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class KingdeeSkuEntity {
    private String fUseOrgId;
    private String fUseOrgName;
    private String fNumber;
    private String fMaterialId;
    private String fName;
    private String fSpecification;
    private String fCreateDate;
    private String fModifyDate;
    private String fDocumentStatus;
    private String fForbidStatus;
    private String fRefStatus;
    private String fPurPrice_CMK;
    private String f_PRVD_Assistant;
    private String f_PRVD_Assistant1;
    private String fSalePrice_CMK;
    private String FSSRQ;
    private String FErpClsID;
}
