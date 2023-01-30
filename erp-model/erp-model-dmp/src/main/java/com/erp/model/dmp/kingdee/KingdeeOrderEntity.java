package com.erp.model.dmp.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class KingdeeOrderEntity {
    private String fID;

    /**
     * 单据编号
     */
    private String fBillNo;

    /**
     * 日期
     */
    private String fDate;

    /**
     * 单据类型
     */
    private String fBillTypeID;

    private String fBillTypeCode;

    /**
     * 单据状态
     */
    private String fDocumentStatus;

    /**
     * 客户id
     */
    private String fCustId;

    /**
     * 销售部门id
     */
    private String fSaleDeptId;

    /**
     * 销售员id
     */
    private String fSalerId;

    /**
     * 收货方地址
     */
    private String fReceiveAddress;

    /**
     * 收货人姓名
     */
    private String fLinkMan;

    /**
     * 联系电话
     */
    private String fLinkPhone;

    /**
     * 审核人id
     */
    private String fApproverId;

    /**
     * 审核日期
     */
    private String fApproveDate;

    /**
     * 关闭状态
     */
    private String fCloseStatus;

    /**
     * 关闭日期
     */
    private String fCloseDate;


    /**
     * 作废状态
     */
    private String fCancelStatus;

    /**
     * 变更人
     */
    private String fChangerId;

    /**
     * 收货方
     */
    private String fReceiveId;


    /**
     * 备注
     */
    private String fNote;

    /**
     * 交货方式
     */
    private String fHeadDeliveryWay;

    /**
     * 交货地点
     */
    private String fHEADLOCID;

    /**
     * 对应组织
     */
    private String fCorrespondOrgId;

    /**
     * 销售组
     */
    private String fSaleGroupId;

    /**
     * 变更原因
     */
    private String fChangeReason;

    /**
     * 业务类型
     */
    private String fBusinessType;

    /**
     * 收货方联系人
     */
    private String fReceiveContact;

    /**
     * 付款方
     */
    private String fChargeId;

    /**
     * 创建人
     */
    private String fCreatorId;

    /**
     * 创建日期
     */
    private String fCreateDate;

    /**
     * 最后修改人
     */
    private String fModifierId;

    /**
     * 最后修改日期
     */
    private String fModifyDate;

    /**
     * 组织id
     */
    private String fSaleOrgId;

    /**
     * 组织名称
     */
    private String fSaleOrgName;


    /**
     * 版本号
     */
    private String fVersionNo;

    /**
     * 签收状态
     */
    private String fSignStatus;

    /**
     * 订单来源
     */
    private String fSOFrom;


    /**
     * 收款日期
     */
    private String f_SK_Date;

    /**
     * 收货国家
     */
    private String fSHGJ1;

    /**
     * 汇率
     */
    private BigDecimal fExchangeRate;

    /**
     * 币别
     */
    private String fSettleCurrId;

    /**
     * 商品信息
     */
    private List<KingdeeOrderItemEntity> orderItemEntityList;
}
