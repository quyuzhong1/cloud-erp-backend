package com.erp.server.dmp.entity.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class KingdeeReturnOrderEntity {
    private String fBillTypeID;
    private String fBillTypeName;
    private String fBillNo;
    private String fOrderNo;
    private String fDate;
    private String fDocumentStatus;
    private String fSaleOrgId;
    private String fSaleOrgName;
    private String fRetcustId;
    private String fRetcustName;
    private String fSalesManId;
    private String fSalesManName;
    private String fCreateDate;
    private String fModifyDate;
    private String fCancelStatus;
    private String fReceiverCountry;
    private String fLinkMan;
    private String fExchangeRate;
    private String fApproveDate;
    private String fBussinessType;
    private String fOwnerTypeIdHead;
    private String fSettleCurrCode;
    private String FDelTime;
    private String FHeadNote;
    private List<KingdeeReturnOrderItemEntity> itemEntityList;
}
