package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class KingdeeSkuEntity {
    @Alias("FUseOrgId")
    private String fUseOrgId;
    @Alias("FUseOrgId.FName")
    private String fUseOrgName;
    @Alias("FNumber")
    private String fNumber;
    @Alias("FMaterialId")
    private String fMaterialId;
    @Alias("FName")
    private String fName;
    @Alias("FSpecification")
    private String fSpecification;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifyDate")
    private String fModifyDate;
    @Alias("FDocumentStatus")
    private String fDocumentStatus;
    @Alias("FForbidStatus")
    private String fForbidStatus;
    @Alias("FRefStatus")
    private String fRefStatus;
    @Alias("FPurPrice_CMK")
    private String fPurPrice_CMK;
    @Alias("F_PRVD_Assistant.FDataValue")
    private String f_PRVD_Assistant;
    @Alias("F_PRVD_Assistant1.FDataValue")
    private String f_PRVD_Assistant1;
    @Alias("FSalePrice_CMK")
    private String fSalePrice_CMK;
    @Alias("F_SSRQ")
    private String FSSRQ;
    @Alias("FErpClsID")
    private String FErpClsID;
}
