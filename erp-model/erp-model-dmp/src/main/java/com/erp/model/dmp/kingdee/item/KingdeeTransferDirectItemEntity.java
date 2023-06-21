package com.erp.model.dmp.kingdee.item;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/6/19 12:21
 **/

@Data
@NoArgsConstructor
@ToString
public class KingdeeTransferDirectItemEntity {
    @Alias("FBillEntry_FEntryID")
    private String FEntryId;

    private String FBillNo;
    /**
     * (明细信息)调出仓库#编码
     */
    private String FSrcStockId;
    /**
     * (明细信息)调出仓库#名称
     */
    @Alias("FSrcStockId.FName")
    private String FSrcStockIdFName;
    /**
     * (明细信息)调入仓库#编码
     */
    private String FDestStockId;
    /**
     * (明细信息)调入仓库#名称
     */
    @Alias("FDestStockId.FName")
    private String FDestStockIdFName;
    /**
     * (明细信息)产品类型
     */
    private String FRowType;
    /**
     * (明细信息)物料编码#编码
     */
    private String FMaterialId;
    @Alias("FMaterialId.FName")
    /**
     * (明细信息)物料编码#名称
     */
    private String FMaterialIdFName;
    /**
     * (明细信息)单位#编码
     */
    private String FUnitID;
    /**
     * (明细信息)单位#名称
     */
    @Alias("FUnitID.FName")
    private String FUnitIDFName;
    /**
     * (明细信息)调拨数量
     */
    private Integer FQty;
    /**
     * (明细信息)调出库存状态#编码
     */
    private String FSrcStockStatusId;
    /**
     * (明细信息)调出库存状态#名称
     */
    @Alias("FSrcStockStatusId.FName")
    private String FSrcStockStatusIdFName;
    /**
     * (明细信息)调入库存状态#编码
     */
    private String FDestStockStatusId;
    /**
     * (明细信息)调入库存状态#名称
     */
    @Alias("FDestStockStatusId.FName")
    private String FDestStockStatusIdFName;
    /**
     * (明细信息)入库日期
     */
    private LocalDateTime FBusinessDate;
    /**
     * (明细信息)赠品
     */
    @Alias("FIsFree")
    private Boolean FIsFree;
    /**
     * (明细信息)调入物料#编码
     */
    private String FDestMaterialId;
    /**
     * (明细信息)调入物料#名称
     */
    @Alias("FDestMaterialId.FName")
    private String FDestMaterialIdFName;
}
