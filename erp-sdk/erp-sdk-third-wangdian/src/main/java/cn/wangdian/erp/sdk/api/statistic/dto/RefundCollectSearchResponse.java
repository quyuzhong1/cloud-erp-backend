package cn.wangdian.erp.sdk.api.statistic.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class RefundCollectSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("detail_list")
	private List<Detail> detailList;

	public static class Detail
	{
		private String brandName ;
		private String className ;
		private String goodsName ;
		private String goodsNo ;
		private String lastStockinTime ;
		private BigDecimal returnAmount;
		private BigDecimal returnNum;
		private String warehouseNo ;
		private String shopNo ;
		private String specCode ;
		private String specName ;
		private String specNo ;
		private BigDecimal stockinNum ;

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public String getLastStockinTime()
		{
			return lastStockinTime;
		}

		public void setLastStockinTime(String lastStockinTime)
		{
			this.lastStockinTime = lastStockinTime;
		}

		public BigDecimal getReturnAmount()
		{
			return returnAmount;
		}

		public void setReturnAmount(BigDecimal returnAmount)
		{
			this.returnAmount = returnAmount;
		}

		public BigDecimal getReturnNum()
		{
			return returnNum;
		}

		public void setReturnNum(BigDecimal returnNum)
		{
			this.returnNum = returnNum;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public BigDecimal getStockinNum()
		{
			return stockinNum;
		}

		public void setStockinNum(BigDecimal stockinNum)
		{
			this.stockinNum = stockinNum;
		}
	}

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<Detail> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<Detail> detailList)
	{
		this.detailList = detailList;
	}
}
