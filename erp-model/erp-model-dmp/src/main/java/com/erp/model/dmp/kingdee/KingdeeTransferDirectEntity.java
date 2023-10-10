package com.erp.model.dmp.kingdee;


import cn.hutool.core.annotation.Alias;
import com.common.business.dto.CleanBaseDTO;
import com.erp.model.dmp.kingdee.item.KingdeeTransferDirectItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 直接调拨单
 * @author Cloud
 */
@Data
@NoArgsConstructor
@ToString
public class KingdeeTransferDirectEntity extends CleanBaseDTO {

    /**
     * 基本信息(序号)
     */
    @Alias("FId")
    private String id;
    /**
     *(基本信息)单据编号
     */
    @Alias("FBillNo")
    private String FBillNo;
    /**
     * (基本信息)业务类型
     */
    @Alias("FBizType")
    private String FBizType;
    /**
     * (基本信息)调拨方向
     */
    @Alias("FTransferDirect")
    private String FTransferDirect;
    /**
     * (基本信息)调拨类型
     */
    @Alias("FTransferBizType")
    private String FTransferBizType;
    /**
     * (基本信息)销售组织#编码
     */
    @Alias("FSaleOrgId")
    private String FSaleOrgId;
    /**
     * (基本信息)销售组织#名称
     */
    @Alias("FSaleOrgId.FName")
    private String FSaleOrgIdFName;
    /**
     * (基本信息)结算组织#编码
     */
    @Alias("FSettleOrgId")
    private String FSettleOrgId;
    /**
     * (基本信息)结算组织#名称
     */
    @Alias("FSettleOrgId.FName")
    private String FSettleOrgIdFName;
    /**
     * (基本信息)调出库存组织#id
     */
    @Alias("FStockOutOrgId")
    private String FStockOutOrgId;

    /**
     * (基本信息)调出库存组织#编码
     */
    @Alias("FStockOutOrgId.FNumber")
    private String FStockOutOrgIdFNumber;

    /**
     * (基本信息)调出库存组织#名称
     */
    @Alias("FStockOutOrgId.FNumber")
    private String FStockOutOrgIdFName;

    /**
     * (基本信息)调出货主#编码
     */
    @Alias("FOwnerOutIdHead")
    private String FOwnerOutIdHead;
    /**
     * (基本信息)调出货主#名称
     */
    @Alias("FOwnerOutIdHead.FName")
    private String FOwnerOutIdHeadFName;
    /**
     * (基本信息)调入库存组织#id
     */
    @Alias("FStockOrgId")
    private String FStockOrgId;

    /**
     * (基本信息)调入库存组织#编码
     */
    @Alias("FStockOrgId.FNumber")
    private String FStockOrgIdFNumber;

    /**
     * (基本信息)调入库存组织#名称
     */
    @Alias("FStockOrgId.FName")
    private String FStockOrgIdFName;
    /**
     * (基本信息)结算币别#编码
     */
    @Alias("FSettleCurrId")
    private String FSettleCurrId;

    @Alias("FSettleCurrId.FName")
    private String FSettleCurrIdFName;
    /**
     * (基本信息)汇率类型#编码
     */
    @Alias("FExchangeTypeId")
    private String FExchangeTypeId;
    /**
     * (基本信息)汇率类型#名称
     */
    @Alias("FExchangeTypeId.FName")
    private String FExchangeTypeIdFName;
    /**
     * (基本信息)汇率
     */
    @Alias("FExchangeRate")
    private String FExchangeRate;
    /**
     * (基本信息)日期
     */
    @Alias("FDate")
    private LocalDateTime FDate;
    /**
     * (基本信息)备注
     */
    @Alias("FNote")
    private String FNote;
    /**
     * (基本信息)本位币#编码
     */
    @Alias("FBaseCurrId")
    private String FBaseCurrId;
    /**
     * (基本信息)本位币#名称
     */
    @Alias("FBaseCurrId.FName")
    private String FBaseCurrIdFName;
    /**
     * 审核状态
     */
    @Alias("FDocumentStatus")
    private String FDocumentStatus;
    /**
     * 审核日期
     */
    @Alias("FApproveDate")
    private LocalDateTime FApproveDate;

    /**
     * 仓管员
     */
    @Alias("FSTOCKERID.FNumber")
    private String  FstockerCode;

    @Alias("FApproverId")
    private String FApproverId;
    /**
     * 审核人
     */
    @Alias("FApproverId.FName")
    private String FApproverIdFName;
    /**
     * 审核状态#名称
     */
    @Alias("FDocumentStatus.FCaption")
    private String FDocumentStatusFName;
    /**
     *创建时间
     */
    @Alias("FCreateDate")
    private LocalDateTime FCreateDate;
    /**
     *创建人
     */
    @Alias("FModifierId")
    private String FModifierId;

    @Alias("FCreatorId")
    private String FCreatorId;

    @Alias("FCreatorId.FName")
    private String FCreatorIdFName;

    @Alias("FModifierId.FName")
    private String FModifierIdFName;
    /**
     *修改时间
     */
    @Alias("FModifyDate")
    private LocalDateTime FModifyDate;

    /**
     * 作废状态
     */
    @Alias("FCancelStatus")
    private String FCancelStatus;

    /**
     * 作废状态
     */
    @Alias("FCancelStatus.FCaption")
    private String FCancelStatusFName;
    /**
     * 作废时间
     */
    @Alias("FCancelDate")
    private LocalDateTime FCancelDate;

    @Alias("FCancellerId")
    private String FCancellerId;

    @Alias("FCancellerId.FName")
    private String FCancellerIdFName;

    /**
     * 直接调拨单详情信息
     */
    private List<KingdeeTransferDirectItemEntity> itemList;


}
