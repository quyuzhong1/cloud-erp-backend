package cn.wangdian.erp.sdk.api.wms.stockpd.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class StockPdQueryDetailResponse
{
	private int total;
	@SerializedName("data")
	private List<StockPdQueryDetailResponse.DetailItem> orders;

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public List<DetailItem> getOrders()
	{
		return orders;
	}

	public void setOrders(List<DetailItem> orders)
	{
		this.orders = orders;
	}

	public static class DetailItem
	{
		@SerializedName("goods_name") private String goodsName;

		@SerializedName("spec_code") private String specCode;

		@SerializedName("batch_no") private String batchNo;

		@SerializedName("brand_no") private String brandNo;
		private String positionNo;
		private String barcode;
		private String modified;
		private String created;

		@SerializedName("goods_no") private String goodsNo;

		@SerializedName("weight") private BigDecimal weight;
		private BigDecimal oldNum;
		private BigDecimal newNum;

		@SerializedName("remark") private String remark;

		@SerializedName("brand_name") private String brandName;

		@SerializedName("goods_count") private BigDecimal goodsCount;

		@SerializedName("rec_id") private int recId;
		private int brandId;
		private int unit;
		private int auxUnit;

		@SerializedName("spec_no") private String specNo;

		private Integer pdId;

		@SerializedName("expire_date") private String expireDate;

		@SerializedName("spec_name") private String specName;

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getBatchNo()
		{
			return batchNo;
		}

		public void setBatchNo(String batchNo)
		{
			this.batchNo = batchNo;
		}

		public String getBrandNo()
		{
			return brandNo;
		}

		public void setBrandNo(String brandNo)
		{
			this.brandNo = brandNo;
		}

		public String getPositionNo()
		{
			return positionNo;
		}

		public void setPositionNo(String positionNo)
		{
			this.positionNo = positionNo;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public BigDecimal getOldNum()
		{
			return oldNum;
		}

		public void setOldNum(BigDecimal oldNum)
		{
			this.oldNum = oldNum;
		}

		public BigDecimal getNewNum()
		{
			return newNum;
		}

		public void setNewNum(BigDecimal newNum)
		{
			this.newNum = newNum;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public int getRecId()
		{
			return recId;
		}

		public void setRecId(int recId)
		{
			this.recId = recId;
		}

		public int getBrandId()
		{
			return brandId;
		}

		public void setBrandId(int brandId)
		{
			this.brandId = brandId;
		}

		public int getUnit()
		{
			return unit;
		}

		public void setUnit(int unit)
		{
			this.unit = unit;
		}

		public int getAuxUnit()
		{
			return auxUnit;
		}

		public void setAuxUnit(int auxUnit)
		{
			this.auxUnit = auxUnit;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public Integer getPdId()
		{
			return pdId;
		}

		public void setPdId(Integer pdId)
		{
			this.pdId = pdId;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}
	}
}
