package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import lombok.Data;
import lombok.ToString;

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

	@ToString
	public static class GoodsList extends CommonCreateBillGoodsReq {

		private String batchNo;
		private String productionDate;
		private String expireDate;

		public String getBatchNo()
		{
			return batchNo;
		}

		public void setBatchNo(String batchNo)
		{
			this.batchNo = batchNo;
		}


		public String getProductionDate()
		{
			return productionDate;
		}

		public void setProductionDate(String productionDate)
		{
			this.productionDate = productionDate;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
		}

	}

	public String getOuterNo()
	{
		return outerNo;
	}

	public void setOuterNo(String outerNo)
	{
		this.outerNo = outerNo;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getLogisticsNo()
	{
		return logisticsNo;
	}

	public void setLogisticsNo(String logisticsNo)
	{
		this.logisticsNo = logisticsNo;
	}

	public String getLogisticsCode()
	{
		return logisticsCode;
	}

	public void setLogisticsCode(String logisticsCode)
	{
		this.logisticsCode = logisticsCode;
	}

	public Boolean getisCheck()
	{
		return isCheck;
	}

	public void setisCheck(Boolean check)
	{
		isCheck = check;
	}

	public List<GoodsList> getGoodsList()
	{
		return goodsList;
	}

	public void setGoodsList(List<GoodsList> goodsList)
	{
		this.goodsList = goodsList;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}
}
