package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class KingdeeDeliveryDetailEntity  extends CleanBaseDTO {

    private String _id;
    @Alias("FID")
    private String fId;

    @Alias("FBillNo")
    private String fBillNo;
    @Alias("FSoorDerno")
    private String fSoorDerno;
    @Alias("FBillTypeID")
    private String fBillTypeID;
    @Alias("FBillTypeID.FName")
    private String fBillTypeName;
    @Alias("FDate")
    private String fDate;
    @Alias("FSaleOrgId")
    private String fSaleOrgId;
    @Alias("FSaleOrgId.FName")
    private String fSaleOrgName;
    @Alias("FCustomerID")
    private String fCustomerID;
    @Alias("FCustomerID.FName")
    private String fCustomerName;
    @Alias("FSaleDeptID.FName")
    private String fSaleDeptName;
    @Alias("FSalesManID")
    private String FSalesManID;
    @Alias("FSalesManID.FName")
    private String FSalesManName;
    @Alias("FReceiverID.FName")
    private String fReceiverName;
    @Alias("FTransferBizType.FName")
    private String fTransferBizTypeName;
    @Alias("F_ulz_BaseProperty2")
    private String f_ulz_BaseProperty2;
    @Alias("F_ulz_BaseProperty2.FNumber")
    private String f_ulz_BaseProperty2Code;

    @Alias("FLinkPhone")
    private String fLinkPhone;
    @Alias("FLinkMan")
    private String fLinkMan;
    @Alias("FBussinessType")
    private String fBussinessType;
    @Alias("FDocumentStatus")
    private String fDocumentStatus;
    @Alias("FNote")
    private String fNote;
    @Alias("FReceiveAddress")
    private String fReceiveAddress;
    @Alias("FCreatorId.FName")
    private String fCreatorName;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifierId.FName")
    private String fModifierName;
    @Alias("FModifyDate")
    private String fModifyDate;
    @Alias("FApproverID.FName")
    private String fApproverName;
    @Alias("FApproveDate")
    private String fApproveDate;
    @Alias("FCancelStatus")
    private String fCancelStatus;
    @Alias("FGYDATE")
    private String fGyDate;
    @Alias("FLogisticsNos")
    private String fLogisticsNos;
    @Alias("F_ulz_Text3")
    private String f_ulz_Text3;
    @Alias("FSettleCurrID.FCode")
    private String fSettleCurrCode;
    @Alias("FExchangeRate")
    private String fExchangeRate;
    private List<KingdeeDeliveryDetailItemEntity> kingdeeOutStockItemEntityList;
    @Override
    public String toString() {
        return "KingdeeDeliveryDetailEntity{" +
                "fId='" + fId + '\'' +
                ", fBillNo='" + fBillNo + '\'' +
                ", fSoorDerno='" + fSoorDerno + '\'' +
                ", fBillTypeID='" + fBillTypeID + '\'' +
                ", fBillTypeName='" + fBillTypeName + '\'' +
                ", fDate='" + fDate + '\'' +
                ", fSaleOrgId='" + fSaleOrgId + '\'' +
                ", fSaleOrgName='" + fSaleOrgName + '\'' +
                ", fCustomerID='" + fCustomerID + '\'' +
                ", fCustomerName='" + fCustomerName + '\'' +
                ", fSaleDeptName='" + fSaleDeptName + '\'' +
                ", FSalesManID='" + FSalesManID + '\'' +
                ", FSalesManName='" + FSalesManName + '\'' +
                ", fReceiverName='" + fReceiverName + '\'' +
                ", fTransferBizTypeName='" + fTransferBizTypeName + '\'' +
                ", f_ulz_BaseProperty2='" + f_ulz_BaseProperty2 + '\'' +
                ", f_ulz_BaseProperty2Code='" + f_ulz_BaseProperty2Code + '\'' +
                ", fLinkPhone='" + fLinkPhone + '\'' +
                ", fLinkMan='" + fLinkMan + '\'' +
                ", fBussinessType='" + fBussinessType + '\'' +
                ", fDocumentStatus='" + fDocumentStatus + '\'' +
                ", fNote='" + fNote + '\'' +
                ", fReceiveAddress='" + fReceiveAddress + '\'' +
                ", fCreatorName='" + fCreatorName + '\'' +
                ", fCreateDate='" + fCreateDate + '\'' +
                ", fModifierName='" + fModifierName + '\'' +
                ", fModifyDate='" + fModifyDate + '\'' +
                ", fApproverName='" + fApproverName + '\'' +
                ", fApproveDate='" + fApproveDate + '\'' +
                ", fCancelStatus='" + fCancelStatus + '\'' +
                ", fGyDate='" + fGyDate + '\'' +
                ", fLogisticsNos='" + fLogisticsNos + '\'' +
                ", f_ulz_Text3='" + f_ulz_Text3 + '\'' +
                ", fSettleCurrCode='" + fSettleCurrCode + '\'' +
                ", fExchangeRate='" + fExchangeRate + '\'' +
                ", kingdeeOutStockItemEntityList=" + kingdeeOutStockItemEntityList +
                '}';
    }
}
