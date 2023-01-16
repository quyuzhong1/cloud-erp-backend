package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class KingdeeShopEntity {
    @Alias("FCUSTID")
    private String fCustId;
    @Alias("FUseOrgId")
    private String fUseOrgId;
    @Alias("FUseOrgId.FNumber")
    private String fUseOrgId_FNumber;
    @Alias("FUseOrgId.FName")
    private String fUseOrgId_FName;
    @Alias("FNumber")
    private String fNumber;
    @Alias("FName")
    private String fName;
    @Alias("FShortName")
    private String fShortName;
    @Alias("FCOUNTRY.FNumber")
    private String fCOUNTRY_FNumber;
    @Alias("FWEBSITE")
    private String fWEBSITE;
    @Alias("FGroup")
    private String fGroup;
    @Alias("FGroup.FNumber")
    private String fGroup_FNumber;
    @Alias("FGroup.FName")
    private String fGroup_FName;
    @Alias("FDescription")
    private String fDescription;
    @Alias("FInvoiceType")
    private String fInvoiceType;
    @Alias("FCustTypeId.FDataValue")
    private String fCustTypeId_FDataValue;
    @Alias("FCustTypeId.FNumber")
    private String fCustTypeId_FNumber;
    @Alias("F_ulz_Assistant.FNumber")
    private String f_ulz_Assistant_FNumber;
    @Alias("F_ulz_Assistant.FDataValue")
    private String f_ulz_Assistant_FDataValue;
    @Alias("FDocumentStatus")
    private String fDocumentStatus;
    @Alias("FForbidStatus")
    private String fForbidStatus;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifyDate")
    private String fModifyDate;
}
