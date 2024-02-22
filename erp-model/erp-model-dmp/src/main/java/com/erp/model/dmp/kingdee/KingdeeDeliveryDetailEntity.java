package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import com.common.business.dto.CleanBaseDTO;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class KingdeeDeliveryDetailEntity  extends CleanBaseDTO {

    private String _id;
    @Alias("FID")
    private String fId;
    /**
     * 单据编号
     */
    @Alias("FBillNo")
    private String fBillNo;
    /**
     * 单据类型
     */
    @Alias("FBillTypeID")
    private String fBillTypeID;
    /**
     * 单据类型名称
     */
    @Alias("FBillTypeID.FName")
    private String fBillTypeName;
    /**
     * 日期
     */
    @Alias("FDate")
    private String fDate;

    /**
     * 运输单号
     */
    @Alias("FCarriageNO")
    private String FCarriageNO;
    /**
     * 销售组织
     */
    @Alias("FSaleOrgId")
    private String fSaleOrgId;
    /**
     * 销售组织名称
     */
    @Alias("FSaleOrgId.FName")
    private String fSaleOrgName;
    /**
     *客户
     */
    @Alias("FCustomerID")
    private String fCustomerID;
    /**
     * 客户名称
     */
    @Alias("FCustomerID.FName")
    private String fCustomerName;

    /**
     * 客户编码
     */
    @Alias("FCustomerID.FNumber")
    private String fCustomerNumber;
    /**
     * 销售部门
     */
    @Alias("FSaleDeptID.FName")
    private String fSaleDeptName;
    /**
     * 原销售员
     */
    @Alias("FSalesManID")
    private String FSalesManID;
    /**
     * 原销售员名称
     */
    @Alias("FSalesManID.FName")
    private String FSalesManName;


    /**
     * 原销售员编号
     */
    @Alias("FSalesManID.FNumber")
    private String FSalesManNumber;

    /**
     * 仓管员编号
     */
    @Alias("FStockerID.FNumber")
    private String  fStockerNumber;

    /**
     * 仓管员
     */
    @Alias("FStockerID.FName")
    private String  fStockerName;
    /**
     * 收货方
     */
    @Alias("FReceiverID.FName")
    private String fReceiverName;
    /**
     * 平台类型
     */
    @Alias("F_ulz_BaseProperty2")
    private String f_ulz_BaseProperty2;
    /**
     * 平台类型编码
     */
    @Alias("F_ulz_BaseProperty2.FNumber")
    private String f_ulz_BaseProperty2Code;

    /**
     * 数据来源
     */
    @Alias("F_ULZ_data_sources")
    private String dataSources;

    /**
     * 联系电话
     */
    @Alias("FLinkPhone")
    private String fLinkPhone;
    /**
     * 联系人
     */
    @Alias("FLinkMan")
    private String fLinkMan;
    /**
     * 业务类型
     */
    @Alias("FBussinessType")
    private String fBussinessType;
    /**
     * 单据状态
     */
    @Alias("FDocumentStatus")
    private String fDocumentStatus;
    /**
     * 收货方地址
     */
    @Alias("FReceiveAddress")
    private String fReceiveAddress;
    /**
     * 创建人
     */
    @Alias("FCreatorId.FName")
    private String fCreatorName;
    /**
     * 创建日期
     */
    @Alias("FCreateDate")
    private String fCreateDate;
    /**
     * 修改人
     */
    @Alias("FModifierId.FName")
    private String fModifierName;
    /**
     * 修改日期
     */
    @Alias("FModifyDate")
    private String fModifyDate;
    /**
     * 审核人
     */
    @Alias("FApproverID.FName")
    private String fApproverName;
    /**
     * 审核日期
     */
    @Alias("FApproveDate")
    private String fApproveDate;
    /**
     * 作废人
     */
    @Alias("FCancelStatus")
    private String fCancelStatus;
    /**
     * 管易发货日期
     */
    @Alias("FGYDATE")
    private String fGyDate;
    /**
     * 物流单号
     */
    @Alias("FLogisticsNos")
    private String fLogisticsNos;
    /**
     * 销售单备注
     */
    @Alias("F_ulz_Text3")
    private String f_ulz_Text3;
    /**
     * 结算币别
     */
    @Alias("FSettleCurrID.FCode")
    private String fSettleCurrCode;

    /**
     * 汇率
     */
    @Alias("FExchangeRate")
    private String fExchangeRate;
    /**
     * 价税合计
     */
    @Alias("FBillAllAmount")
    private BigDecimal fBillAllAmount;

    /**
     * 价税合计（本位币）
     */
    @Alias("FBillAllAmount_LC")
    private BigDecimal fBillAllAmount_LC;

    @Alias("FEntryCostAmount")
    private String fEntryCostAmount;
    @Alias("FISGENFORIOS")
    private Boolean FIsGenForIos;

    /**
     * 第三方单据编号
     */
    @Alias("FETHIRDBILLNO")
    private String fEThirdBillNo;

    private List<KingdeeDeliveryDetailItemEntity> kingdeeOutStockItemEntityList;

    @Override
    public String toString() {
        return "KingdeeDeliveryDetailEntity{" +
                "fId='" + fId + '\'' +
                ", fBillNo='" + fBillNo + '\'' +
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
                ", f_ulz_BaseProperty2='" + f_ulz_BaseProperty2 + '\'' +
                ", f_ulz_BaseProperty2Code='" + f_ulz_BaseProperty2Code + '\'' +
                ", fLinkPhone='" + fLinkPhone + '\'' +
                ", fLinkMan='" + fLinkMan + '\'' +
                ", fBussinessType='" + fBussinessType + '\'' +
                ", fDocumentStatus='" + fDocumentStatus + '\'' +
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
                ", FIsGenForIos='" + FIsGenForIos + '\'' +
                ", kingdeeOutStockItemEntityList=" + kingdeeOutStockItemEntityList +
                '}';
    }
}
