package com.sdk.wangdian.sdk.api.wms.stockout.dto;

import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOtherStockoutRequest
{
	private String outerNo;
	private String warehouseNo;
	private String logisticsNo;
	private String logisticsCode;
	private BigDecimal postFee;
	private Boolean isCheck;
	private List<CreateOtherStockoutRequest.GoodsList> goodsList;
	private String remark;
	private String reason;
	private String dmpSyncTaskId;

	/**
	 * 来源单据编码
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

	/**
	 * 来源单据号
	 */
	private String sourceCode;

	@EqualsAndHashCode(callSuper = true)
	@ToString
	@Data
	public static class GoodsList extends CommonCreateBillGoodsReq {


	}
}
