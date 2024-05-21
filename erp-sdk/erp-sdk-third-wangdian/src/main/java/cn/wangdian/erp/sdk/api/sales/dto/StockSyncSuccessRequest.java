package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class StockSyncSuccessRequest
{
	public static class SyncInfo
	{
		@SerializedName("syn_result")
		private String synResult;

		@SerializedName("stock_syn_other")
		private String stockSynOther;

		@SerializedName("stock_syn_min")
		private BigDecimal stockSynMin;

		@SerializedName("stock_syn_rule_id")
		private Integer stockSynRuleId;

		@SerializedName("stock_syn_mask")
		private Integer stockSynMask;

		@SerializedName("stock_syn_warehouses")
		private String stockSynWarehouses;

		@SerializedName("is_syn_success")
		private Boolean isSynSuccess;

		@SerializedName("is_manual")
		private Boolean isManual;

		@SerializedName("stock_syn_max")
		private BigDecimal stockSynMax;

		@SerializedName("stock_syn_percent")
		private Integer stockSynPercent;

		@SerializedName("is_auto_delisting")
		private Boolean isAutoDelisting;

		@SerializedName("stock_syn_plus")
		private BigDecimal stockSynPlus;

		@SerializedName("is_auto_listing")
		private Boolean isAutoListing;

		@SerializedName("stock_syn_rule_no")
		private String stockSynRuleNo;

		@SerializedName("syn_stock")
		private BigDecimal synStock;

		@SerializedName("stock_change_count")
		private Integer stockChangeCount;

		public String getSynResult()
		{
			return synResult;
		}

		public void setSynResult(String synResult)
		{
			this.synResult = synResult;
		}

		public String getStockSynOther()
		{
			return stockSynOther;
		}

		public void setStockSynOther(String stockSynOther)
		{
			this.stockSynOther = stockSynOther;
		}

		public BigDecimal getStockSynMin()
		{
			return stockSynMin;
		}

		public void setStockSynMin(BigDecimal stockSynMin)
		{
			this.stockSynMin = stockSynMin;
		}

		public Integer getStockSynRuleId()
		{
			return stockSynRuleId;
		}

		public void setStockSynRuleId(Integer stockSynRuleId)
		{
			this.stockSynRuleId = stockSynRuleId;
		}

		public Integer getStockSynMask()
		{
			return stockSynMask;
		}

		public void setStockSynMask(Integer stockSynMask)
		{
			this.stockSynMask = stockSynMask;
		}

		public String getStockSynWarehouses()
		{
			return stockSynWarehouses;
		}

		public void setStockSynWarehouses(String stockSynWarehouses)
		{
			this.stockSynWarehouses = stockSynWarehouses;
		}

		public Boolean getSynSuccess()
		{
			return isSynSuccess;
		}

		public void setSynSuccess(Boolean synSuccess)
		{
			isSynSuccess = synSuccess;
		}

		public Boolean getManual()
		{
			return isManual;
		}

		public void setManual(Boolean manual)
		{
			isManual = manual;
		}

		public BigDecimal getStockSynMax()
		{
			return stockSynMax;
		}

		public void setStockSynMax(BigDecimal stockSynMax)
		{
			this.stockSynMax = stockSynMax;
		}

		public Integer getStockSynPercent()
		{
			return stockSynPercent;
		}

		public void setStockSynPercent(Integer stockSynPercent)
		{
			this.stockSynPercent = stockSynPercent;
		}

		public Boolean getAutoDelisting()
		{
			return isAutoDelisting;
		}

		public void setAutoDelisting(Boolean autoDelisting)
		{
			isAutoDelisting = autoDelisting;
		}

		public BigDecimal getStockSynPlus()
		{
			return stockSynPlus;
		}

		public void setStockSynPlus(BigDecimal stockSynPlus)
		{
			this.stockSynPlus = stockSynPlus;
		}

		public Boolean getAutoListing()
		{
			return isAutoListing;
		}

		public void setAutoListing(Boolean autoListing)
		{
			isAutoListing = autoListing;
		}

		public String getStockSynRuleNo()
		{
			return stockSynRuleNo;
		}

		public void setStockSynRuleNo(String stockSynRuleNo)
		{
			this.stockSynRuleNo = stockSynRuleNo;
		}

		public BigDecimal getSynStock()
		{
			return synStock;
		}

		public void setSynStock(BigDecimal synStock)
		{
			this.synStock = synStock;
		}

		public Integer getStockChangeCount()
		{
			return stockChangeCount;
		}

		public void setStockChangeCount(Integer stockChangeCount)
		{
			this.stockChangeCount = stockChangeCount;
		}
	}
}
