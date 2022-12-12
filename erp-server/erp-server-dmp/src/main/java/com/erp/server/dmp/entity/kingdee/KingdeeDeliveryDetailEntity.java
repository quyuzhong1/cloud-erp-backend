package com.erp.server.dmp.entity.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class KingdeeDeliveryDetailEntity {
    private String fBillTypeID;
    private String fBillTypeName;
    private String fBillNo;
    private String fSoorDerno;
    private String fDate;
    private String fSaleOrgId;
    private String fSaleOrgName;
    private String fCustomerID;
    private String fCustomerName;
    private String fSaleDeptName;
    private String FSalesManID;
    private String FSalesManName;
    private String fReceiverName;
    private String fTransferBizTypeName;
    private String f_ulz_BaseProperty2;
    private String fLinkPhone;
    private String fLinkMan;
    private String fBussinessType;
    private String fDocumentStatus;
    private String fNote;
    private String fReceiveAddress;
    private String fCreatorName;
    private String fCreateDate;
    private String fModifierName;
    private String fModifyDate;
    private String fApproverName;
    private String fApproveDate;
    private String fCancelStatus;
    private String fGYDATE;
    private String fLogisticsNos;
    private String f_ulz_Text3;
    private String fSettleCurrCode;
    private String fExchangeRate;
    private List<KingdeeDeliveryDetailItemEntity> kingdeeOutStockItemEntityList;
}
