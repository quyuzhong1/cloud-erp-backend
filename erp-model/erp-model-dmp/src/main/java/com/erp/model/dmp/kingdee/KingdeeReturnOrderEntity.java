package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class KingdeeReturnOrderEntity {

    private String _id;
    @Alias("FBillTypeID")
    private String fBillTypeID;
    @Alias("FBillTypeID.FName")
    private String fBillTypeName;
    @Alias("FBillTypeID.FNumber")
    private String fBillTypeCode;
    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FOrderNo")
    private String fOrderNo;
    @Alias("FDate")
    private String fDate;
    @Alias("FDocumentStatus")
    private String fDocumentStatus;
    @Alias("FSaleOrgId")
    private String fSaleOrgId;
    @Alias("FSaleOrgId.FName")
    private String fSaleOrgName;
    @Alias("FRecustId")
    private String fRetcustId;
    @Alias("FRetcustId.FName")
    private String fRetcustName;
    @Alias("FSalesManId")
    private String fSalesManId;
    @Alias("FSalesManId.FName")
    private String fSalesManName;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifyDate")
    private String fModifyDate;
    @Alias("FCancelStatus")
    private String fCancelStatus;
    @Alias("FReceiverCountry")
    private String fReceiverCountry;
    @Alias("FLinkMan")
    private String fLinkMan;
    @Alias("FExchangeRate")
    private BigDecimal fExchangeRate;
    @Alias("FApproveDate")
    private String fApproveDate;
    @Alias("FBussinessType")
    private String fBussinessType;
    @Alias("FOwnerTypeIdHead")
    private String fOwnerTypeIdHead;
    @Alias("FSettleCurrId.FCode")
    private String fSettleCurrCode;
    @Alias("FDelTime")
    private String FDelTime;
    @Alias("FHeadNote")
    private String FHeadNote;
    private List<KingdeeReturnOrderItemEntity> itemEntityList;
}
