package com.erp.model.dmp.kingdee;


import cn.hutool.core.annotation.Alias;
import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.kingdee.item.KingdeeTransferDirectItemEntity;
import com.google.gson.annotations.SerializedName;
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
    @SerializedName("FId")
    private String id;
    /**
     *(基本信息)单据编号
     */
    private String FBillNo;
    /**
     * (基本信息)业务类型
     */
    private String FBizType;
    /**
     * (基本信息)调拨方向
     */
    private String FTransferDirect;
    /**
     * (基本信息)调拨类型
     */
    private String FTransferBizType;
    /**
     * (基本信息)销售组织#编码
     */
    private String FSaleOrgId;
    /**
     * (基本信息)销售组织#名称
     */
    @Alias("FSaleOrgId.FName")
    private String FSaleOrgIdFName;
    /**
     * (基本信息)结算组织#编码
     */
    private String FSettleOrgId;
    /**
     * (基本信息)结算组织#名称
     */
    @Alias("FSettleOrgId.FName")
    private String FSettleOrgIdFName;
    /**
     * (基本信息)调出库存组织#编码
     */
    private String FStockOutOrgId;
    @Alias("FStockOutOrgId.FName")
    /**
     * (基本信息)调出库存组织#名称
     */
    private String FStockOutOrgIdFName;
    /**
     * (基本信息)调出货主#编码
     */
    private String FOwnerOutIdHead;
    /**
     * (基本信息)调出货主#名称
     */
    @Alias("FOwnerOutIdHead.FName")
    private String FOwnerOutIdHeadFName;
    /**
     * (基本信息)调入库存组织#编码
     */
    private String FStockOrgId;
    /**
     * (基本信息)调入库存组织#名称
     */
    @Alias("FStockOrgId.FName")
    private String FStockOrgIdFName;
    /**
     * (基本信息)结算币别#编码
     */
    private String FSettleCurrId;

    @Alias("FSettleCurrId.FName")
    private String FSettleCurrIdFName;
    /**
     * (基本信息)汇率类型#编码
     */
    private String FExchangeTypeId;
    /**
     * (基本信息)汇率类型#名称
     */
    @Alias("FExchangeTypeId.FName")
    private String FExchangeTypeIdFName;
    /**
     * (基本信息)汇率
     */
    private String FExchangeRate;
    /**
     * (基本信息)日期
     */
    private LocalDateTime FDate;
    /**
     * (基本信息)备注
     */
    private String FNote;
    /**
     * (基本信息)本位币#编码
     */
    private String FBaseCurrId;
    /**
     * (基本信息)本位币#名称
     */
    @Alias("FBaseCurrId.FName")
    private String FBaseCurrIdFName;
    /**
     * 审核状态
     */
    private String FDocumentStatus;
    /**
     * 审核日期
     */
    private LocalDateTime FApproveDate;

    /**
     * 仓管员
     */
    @Alias("FSTOCKERID.FNumber")
    private String  FstockerCode;

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
    private LocalDateTime FCreateDate;
    /**
     *创建人
     */
    private String FModifierId;

    private String FCreatorId;

    @Alias("FCreatorId.FName")
    private String FCreatorIdFName;

    @Alias("FModifierId.FName")
    private String FModifierIdFName;
    /**
     *修改时间
     */
    private LocalDateTime FModifyDate;

    /**
     * 作废状态
     */
    private String FCancelStatus;

    /**
     * 作废状态
     */
    @Alias("FCancelStatus.FCaption")
    private String FCancelStatusFName;
    /**
     * 作废时间
     */
    private LocalDateTime FCancelDate;

    private String FCancellerId;

    @Alias("FCancellerId.FName")
    private String FCancellerIdFName;

    /**
     * 直接调拨单详情信息
     */
    private List<KingdeeTransferDirectItemEntity> itemList;


}
