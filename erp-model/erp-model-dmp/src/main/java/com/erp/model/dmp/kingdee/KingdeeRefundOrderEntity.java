package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class KingdeeRefundOrderEntity {
    @Alias("FID")
    private String fId;
    @Alias("FBillTypeID")
    private String fBillTypeID;
    @Alias("FBillTypeName")
    private String fBillTypeName;
    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FDATE")
    private String fDate;
    @Alias("FSETTLERATE")
    private String fSettleRate;
    @Alias("FREFUNDAMOUNTFOR_H")
    private String fRefundAmountForH;
    @Alias("FDOCUMENTSTATUS")
    private String fDocumentStatus;
    @Alias("FRECTUNIT")
    private String fRectUnit;
    @Alias("FRECTUNITName")
    private String fRectUnitName;
    @Alias("FSETTLECURCode")
    private String fSettleCurCode;
    @Alias("FREALREFUNDAMOUNTFOR")
    private String fRealRefundAmountFor;
    @Alias("FEXCHANGERATE")
    private String fExchangeRate;
    @Alias("FWRITTENOFFSTATUS")
    private String fWrittenOffStatus;
    @Alias("FCancelStatus")
    private String fCancelStatus;
    @Alias("FREMARK")
    private String fRemark;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifyDate")
    private String fModifyDate;
    @Alias("FApproveDate")
    private String fApproveDate;
    @Alias("FWBSETTLENO")
    private String fWbSettleNo;
    @Alias("FCountry")
    private String fCountry;
    @Alias("FSALEORGName")
    private String fSaleOrgName;
    @Alias("FSALEORGID")
    private String fSaleOrgId;
    @Alias("FSALEERID")
    private String fSaleErId;
    @Alias("FSALEERName")
    private String fSaleErName;
}
