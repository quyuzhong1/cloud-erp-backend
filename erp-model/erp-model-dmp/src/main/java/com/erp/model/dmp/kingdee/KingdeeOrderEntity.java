package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class KingdeeOrderEntity {
    @Alias("FID")
    private String fID;

    /**
     * 单据编号
     */
    @Alias("FBillNo")
    private String fBillNo;

    /**
     * 日期
     */
    @Alias("FDate")
    private String fDate;

    /**
     * 单据类型
     */
    @Alias("FBillTypeId.FName")
    private String fBillTypeID;
    @Alias("FBillTypeId.FNumber")
    private String fBillTypeCode;

    /**
     * 单据状态
     */
    @Alias("FDocumentStatus")
    private String fDocumentStatus;

    /**
     * 客户id
     */
    @Alias("FCustId.FName")
    private String fCustId;

    /**
     * 销售部门id
     */
    @Alias("FSaleDeptId.FName")
    private String fSaleDeptId;

    /**
     * 销售员id
     */
    @Alias("FSalerId.FName")
    private String fSalerId;

    /**
     * 收货方地址
     */
    @Alias("FReceiveAddress")
    private String fReceiveAddress;

    /**
     * 收货人姓名
     */
    @Alias("FLinkMan")
    private String fLinkMan;

    /**
     * 联系电话
     */
    @Alias("FLinkPhone")
    private String fLinkPhone;

    /**
     * 审核人id
     */
    @Alias("FApproverId.FName")
    private String fApproverId;

    /**
     * 审核日期
     */
    @Alias("FApproveDate")
    private String fApproveDate;

    /**
     * 关闭状态
     */
    @Alias("FCloseStatus")
    private String fCloseStatus;

    /**
     * 关闭日期
     */
    @Alias("FCloseDate")
    private String fCloseDate;


    /**
     * 作废状态
     */
    @Alias("FCancelStatus")
    private String fCancelStatus;

    /**
     * 变更人
     */
    @Alias("FChangerId")
    private String fChangerId;

    /**
     * 收货方
     */
    @Alias("FReceiveId.FName")
    private String fReceiveId;


    /**
     * 备注
     */
    @Alias("FNote")
    private String fNote;

    /**
     * 交货方式
     */
    @Alias("FHeadDeliveryWay")
    private String fHeadDeliveryWay;

    /**
     * 交货地点
     */
    @Alias("FHEADLOCID")
    private String fHEADLOCID;

    /**
     * 对应组织
     */
    @Alias("FCorrespondOrgId")
    private String fCorrespondOrgId;

    /**
     * 销售组
     */
    @Alias("FSaleGroupId")
    private String fSaleGroupId;

    /**
     * 变更原因
     */
    @Alias("FChangeReason")
    private String fChangeReason;

    /**
     * 业务类型
     */
    @Alias("FBussinessType")
    private String fBussinessType;

    /**
     * 收货方联系人
     */
    @Alias("FReceiveContact")
    private String fReceiveContact;

    /**
     * 付款方
     */
    @Alias("FChargeId")
    private String fChargeId;

    /**
     * 创建人
     */
    @Alias("FCreatorId")
    private String fCreatorId;

    /**
     * 创建日期
     */
    @Alias("FCreateDate")
    private String fCreateDate;

    /**
     * 最后修改人
     */
    @Alias("FModifierId")
    private String fModifierId;

    /**
     * 最后修改日期
     */
    @Alias("FModifyDate")
    private String fModifyDate;

    /**
     * 组织id
     */
    @Alias("FSaleOrgId")
    private String fSaleOrgId;

    /**
     * 组织名称
     */
    @Alias("FSaleOrgId.FName")
    private String fSaleOrgName;


    /**
     * 版本号
     */
    @Alias("FVersionNo")
    private String fVersionNo;

    /**
     * 签收状态
     */
    @Alias("FSignStatus")
    private String fSignStatus;

    /**
     * 订单来源
     */
    @Alias("FSOFrom")
    private String fSOFrom;


    /**
     * 收款日期
     */
    @Alias("F_SK_Date")
    private String f_SK_Date;

    /**
     * 收货国家
     */
    @Alias("F_SHGJ1")
    private String fSHGJ1;

    /**
     * 汇率
     */
    @Alias("FExchangeRate")
    private BigDecimal fExchangeRate;

    /**
     * 币别
     */
    @Alias("FSettleCurrId.FCode")
    private String fSettleCurrId;

    /**
     * 商品信息
     */
    private List<KingdeeOrderItemEntity> orderItemEntityList;
}
