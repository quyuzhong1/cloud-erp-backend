package com.erp.model.dmp.kingdee;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString
public class KingdeeReturnOrderEntity extends CleanBaseDTO {

    private String _id;
    @JSONField(name = "FID")
    @JsonProperty("FID")
    private String fId;
    @JSONField(name = "FBillTypeID")
    @JsonProperty("FBillTypeID.FNumber")
    private String fBillTypeID;
    @JSONField(name = "FBillTypeID.FName")
    @JsonProperty("FBillTypeID.FNumber")
    private String fBillTypeName;
    @JSONField(name = "FBillTypeID.FNumber")
    @JsonProperty("FBillTypeID.FNumber")
    private String fBillTypeCode;
    @JSONField(name = "FBillNo")
    @JsonProperty("FBillNo")
    private String fBillNo;
    @JSONField(name = "FDate")
    @JsonProperty("FDate")
    private String fDate;
    @JSONField(name = "FDocumentStatus")
    @JsonProperty("FDocumentStatus")
    private String fDocumentStatus;
    @JSONField(name = "FSaleOrgId")
    @JsonProperty("FSaleOrgId")
    private String fSaleOrgId;
    @JSONField(name = "FSaleOrgId.FName")
    @JsonProperty("FSaleOrgId.FName")
    private String fSaleOrgName;
    @JSONField(name = "FRetcustId.FName")
    @JsonProperty("FRetcustId.FName")
    private String fRetcustName;
    @JSONField(name = "FRetcustId.FNumber")
    @JsonProperty("FRetcustId.FNumber")
    private String FRetcustNumber;
    @JSONField(name = "FSalesManId")
    @JsonProperty("FSalesManId")
    private String fSalesManId;
    @JSONField(name = "FSalesManId.FName")
    @JsonProperty("FSalesManId.FName")
    private String fSalesManName;
    @JSONField(name = "FCreateDate")
    @JsonProperty("FCreateDate")
    private String fCreateDate;
    @JSONField(name = "FModifyDate")
    @JsonProperty("FModifyDate")
    private String fModifyDate;
    @JSONField(name = "FCancelStatus")
    @JsonProperty("FCancelStatus")
    private String fCancelStatus;
    @JSONField(name = "FReceiverCountry")
    @JsonProperty("FReceiverCountry")
    private String fReceiverCountry;
    @JSONField(name = "FLinkMan")
    @JsonProperty("FLinkMan")
    private String fLinkMan;
    @JSONField(name = "FExchangeRate")
    @JsonProperty("FExchangeRate")
    private BigDecimal fExchangeRate;
    @JSONField(name = "FApproveDate")
    @JsonProperty("FApproveDate")
    private String fApproveDate;
    @JSONField(name = "FBussinessType")
    @JsonProperty("FBussinessType")
    private String fBussinessType;
    @JSONField(name = "FOwnerTypeIdHead")
    @JsonProperty("FOwnerTypeIdHead")
    private String fOwnerTypeIdHead;
    @JSONField(name = "FSettleCurrId.FCode")
    @JsonProperty("FSettleCurrId.FCode")
    private String fSettleCurrCode;
    @JSONField(name = "FDelTime")
    @JsonProperty("FDelTime")
    private String FDelTime;
    @JSONField(name = "FHeadNote")
    @JsonProperty("FHeadNote")
    private String FHeadNote;
    @JSONField(name = "FReturnReason")
    @JsonProperty("FReturnReason")
    private String fReturnReason;
    @JSONField(name = "FSaledeptid.FNumber")
    @JsonProperty("FSaledeptid.FNumber")
    private String fSaledeptNumber;
    @JSONField(name = "FSaledeptid.FName")
    @JsonProperty("FSaledeptid.FName")
    private String fSaledeptName;
    @JSONField(name = "F_ULZ_data_sources")
    @JsonProperty("F_ULZ_data_sources")
    private String fULZDataSources;
    @JSONField(name = "FISGENFORIOS")
    @JsonProperty("FISGENFORIOS")
    private Boolean fIsGenForIos;
    @JSONField(name = "FETHIRDBILLNO")
    @JsonProperty("FETHIRDBILLNO")
    private String fEThirdBillNo;

    /**
     * 是否无效 false：无效  true：有效
     */
    private Boolean isValid;

    private List<KingdeeReturnOrderItemEntity> itemEntityList;

    public KingdeeReturnOrderEntity() {
        this.isValid = Boolean.TRUE;
    }

    @Override
    public String toString() {
        return "KingdeeReturnOrderEntity{" +
                "fBillTypeID='" + fBillTypeID + '\'' +
                ", fBillTypeName='" + fBillTypeName + '\'' +
                ", fBillTypeCode='" + fBillTypeCode + '\'' +
                ", fBillNo='" + fBillNo + '\'' +
                ", fDate='" + fDate + '\'' +
                ", fDocumentStatus='" + fDocumentStatus + '\'' +
                ", fSaleOrgId='" + fSaleOrgId + '\'' +
                ", fSaleOrgName='" + fSaleOrgName + '\'' +
                ", fRetcustName='" + fRetcustName + '\'' +
                ", fSalesManId='" + fSalesManId + '\'' +
                ", fSalesManName='" + fSalesManName + '\'' +
                ", fCreateDate='" + fCreateDate + '\'' +
                ", fModifyDate='" + fModifyDate + '\'' +
                ", fCancelStatus='" + fCancelStatus + '\'' +
                ", fReceiverCountry='" + fReceiverCountry + '\'' +
                ", fLinkMan='" + fLinkMan + '\'' +
                ", fExchangeRate=" + fExchangeRate +
                ", fApproveDate='" + fApproveDate + '\'' +
                ", fBussinessType='" + fBussinessType + '\'' +
                ", fOwnerTypeIdHead='" + fOwnerTypeIdHead + '\'' +
                ", fSettleCurrCode='" + fSettleCurrCode + '\'' +
                ", FDelTime='" + FDelTime + '\'' +
                ", FHeadNote='" + FHeadNote + '\'' +
                ", itemEntityList=" + itemEntityList +
                '}';
    }
}


