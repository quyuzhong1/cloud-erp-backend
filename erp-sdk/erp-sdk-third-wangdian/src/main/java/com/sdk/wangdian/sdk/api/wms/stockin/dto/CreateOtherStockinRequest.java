package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOtherStockinRequest
{
	private String outerNo;
	private String warehouseNo;
	private String logisticsNo;
	private String logisticsCode;
	private Boolean isCheck;
    private List<GoodsList> goodsList;
    private String remark;
    private String reason;
	private String dmpSyncTaskId;

	/**
	 * 来源单据Id
	 */
	private String sourceId;

	/**
	 * 操作方向: 审核(operateApprove) / 反审核(operateDisApprove)
	 * @see SyncOperateEnum
	 */
	private String operateCode;

	/**
	 * 来源平台名称: 自研ERP
	 * @see PlatformEnum
	 */
	private String sourcePlatformName;

	/**
	 * 目标平台名称
	 * @see PlatformEnum
	 */
	private String targetPlatformName;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * ERP仓库ID
	 */
	private String sysWarehouseId;

	@EqualsAndHashCode(callSuper = true)
	@ToString
	@Data
	public static class GoodsList extends CommonCreateBillGoodsReq {
		private String batchNo;
		private String productionDate;
		private String expireDate;
	}
}
