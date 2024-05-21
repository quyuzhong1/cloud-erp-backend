package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class GoodsSuiteSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("suite_list")
	private List<GoodsSuiteDto> GoodsSuiteList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<GoodsSuiteDto> getGoodsSuiteList()
	{
		return GoodsSuiteList;
	}

	public void setGoodsSuiteList(List<GoodsSuiteDto> goodsSuiteList)
	{
		GoodsSuiteList = goodsSuiteList;
	}

	public static class GoodsSuiteDto
	{
		@SerializedName("suite_id")
		private Integer suiteId;
		@SerializedName("suite_no")
		private String suiteNo;
		@SerializedName("suite_name")
		private String suiteName;
		@SerializedName("weight")
		private BigDecimal weight;
		@SerializedName("number_price")
		private BigDecimal numberPrice;
		@SerializedName("remark")
		private String remark;
		@SerializedName("brand_name")
		private String brandName;
		@SerializedName("retail_price")
		private BigDecimal retailPrice;
		@SerializedName("prop4")
		private String prop4;
		@SerializedName("prop3")
		private String prop3;
		@SerializedName("prop2")
		private String prop2;
		@SerializedName("prop1")
		private String prop1;
		@SerializedName("flag_name")
		private String flagName;
		@SerializedName("unit_name")
		private String unitName;
		@SerializedName("aux_unit_name")
		private String auxUnitName;
		@SerializedName("wholesale_price")
		private BigDecimal wholesalePrice;
		@SerializedName("marketPrice")
		private BigDecimal marketPrice;
		@SerializedName("short_name")
		private String shortName;
		@SerializedName("goods_label")
		private String goodsLabel;
		@SerializedName("barcode")
		private String barcode;
		@SerializedName("class_name")
		private String className;
		@SerializedName("print_suite_mode")
		private Boolean printSuiteMode;
		@SerializedName("deleted")
		private Integer deleted;
		@SerializedName("suite_modified")
		private String suiteModified;
		@SerializedName("suite_created")
		private String suiteCreated;
		@SerializedName("barcode_count")
		private String barcodeCount;
		@SerializedName("detail_list")
		private List<GoodsSuiteDetailDto> GoodsSuitedetailList;

		public Integer getSuiteId()
		{
			return suiteId;
		}

		public void setSuiteId(Integer suiteId)
		{
			this.suiteId = suiteId;
		}

		public String getSuiteNo()
		{
			return suiteNo;
		}

		public void setSuiteNo(String suiteNo)
		{
			this.suiteNo = suiteNo;
		}

		public String getSuiteName()
		{
			return suiteName;
		}

		public void setSuiteName(String suiteName)
		{
			this.suiteName = suiteName;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public BigDecimal getNumberPrice()
		{
			return numberPrice;
		}

		public void setNumberPrice(BigDecimal numberPrice)
		{
			this.numberPrice = numberPrice;
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

		public BigDecimal getRetailPrice()
		{
			return retailPrice;
		}

		public void setRetailPrice(BigDecimal retailPrice)
		{
			this.retailPrice = retailPrice;
		}

		public String getProp4()
		{
			return prop4;
		}

		public void setProp4(String prop4)
		{
			this.prop4 = prop4;
		}

		public String getProp3()
		{
			return prop3;
		}

		public void setProp3(String prop3)
		{
			this.prop3 = prop3;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getFlagName()
		{
			return flagName;
		}

		public void setFlagName(String flagName)
		{
			this.flagName = flagName;
		}

		public String getUnitName()
		{
			return unitName;
		}

		public void setUnitName(String unitName)
		{
			this.unitName = unitName;
		}

		public String getAuxUnitName()
		{
			return auxUnitName;
		}

		public void setAuxUnitName(String auxUnitName)
		{
			this.auxUnitName = auxUnitName;
		}

		public BigDecimal getWholesalePrice()
		{
			return wholesalePrice;
		}

		public void setWholesalePrice(BigDecimal wholesalePrice)
		{
			this.wholesalePrice = wholesalePrice;
		}

		public BigDecimal getMarketPrice()
		{
			return marketPrice;
		}

		public void setMarketPrice(BigDecimal marketPrice)
		{
			this.marketPrice = marketPrice;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public String getGoodsLabel()
		{
			return goodsLabel;
		}

		public void setGoodsLabel(String goodsLabel)
		{
			this.goodsLabel = goodsLabel;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public Boolean getPrintSuiteMode()
		{
			return printSuiteMode;
		}

		public void setPrintSuiteMode(Boolean printSuiteMode)
		{
			this.printSuiteMode = printSuiteMode;
		}

		public Integer getDeleted()
		{
			return deleted;
		}

		public void setDeleted(Integer deleted)
		{
			this.deleted = deleted;
		}

		public String getSuiteModified()
		{
			return suiteModified;
		}

		public void setSuiteModified(String suiteModified)
		{
			this.suiteModified = suiteModified;
		}

		public String getSuiteCreated()
		{
			return suiteCreated;
		}

		public void setSuiteCreated(String suiteCreated)
		{
			this.suiteCreated = suiteCreated;
		}

		public String getBarcodeCount()
		{
			return barcodeCount;
		}

		public void setBarcodeCount(String barcodeCount)
		{
			this.barcodeCount = barcodeCount;
		}

		public List<GoodsSuiteDetailDto> getGoodsSuitedetailList()
		{
			return GoodsSuitedetailList;
		}

		public void setGoodsSuitedetailList(List<GoodsSuiteDetailDto> goodsSuitedetailList)
		{
			GoodsSuitedetailList = goodsSuitedetailList;
		}

		public static class GoodsSuiteDetailDto
		{
			@SerializedName("rec_id")
			private Integer recId;
			@SerializedName("suite_id")
			private Integer suiteId;
			@SerializedName("spec_no")
			private String specNo;
			@SerializedName("spec_id")
			private Integer specId;
			@SerializedName("unit")
			private Integer unit;
			@SerializedName("deleted")
			private Integer deleted;
			@SerializedName("spec_name")
			private String specName;
			@SerializedName("spec_code")
			private String specCode;
			@SerializedName("barcode")
			private String barcode;
			@SerializedName("goods_no")
			private String goodsNo;
			@SerializedName("goods_id")
			private Integer goodsId;
			@SerializedName("goods_name")
			private String goodsName;
			@SerializedName("num")
			private BigDecimal num;
			@SerializedName("fixed_price")
			private BigDecimal fixedPrice;
			@SerializedName("ratio")
			private BigDecimal ratio;
			@SerializedName("is_fixed_price")
			private Boolean isFixedPrice;
			@SerializedName("modified")
			private String modified;
			@SerializedName("created")
			private String created;

			public Integer getRecId()
			{
				return recId;
			}

			public void setRecId(Integer recId)
			{
				this.recId = recId;
			}

			public Integer getSuiteId()
			{
				return suiteId;
			}

			public void setSuiteId(Integer suiteId)
			{
				this.suiteId = suiteId;
			}

			public String getSpecNo()
			{
				return specNo;
			}

			public void setSpecNo(String specNo)
			{
				this.specNo = specNo;
			}

			public Integer getSpecId()
			{
				return specId;
			}

			public void setSpecId(Integer specId)
			{
				this.specId = specId;
			}

			public Integer getUnit()
			{
				return unit;
			}

			public void setUnit(Integer unit)
			{
				this.unit = unit;
			}

			public Integer getDeleted()
			{
				return deleted;
			}

			public void setDeleted(Integer deleted)
			{
				this.deleted = deleted;
			}

			public String getSpecName()
			{
				return specName;
			}

			public void setSpecName(String specName)
			{
				this.specName = specName;
			}

			public String getSpecCode()
			{
				return specCode;
			}

			public void setSpecCode(String specCode)
			{
				this.specCode = specCode;
			}

			public String getBarcode()
			{
				return barcode;
			}

			public void setBarcode(String barcode)
			{
				this.barcode = barcode;
			}

			public String getGoodsNo()
			{
				return goodsNo;
			}

			public void setGoodsNo(String goodsNo)
			{
				this.goodsNo = goodsNo;
			}

			public Integer getGoodsId()
			{
				return goodsId;
			}

			public void setGoodsId(Integer goodsId)
			{
				this.goodsId = goodsId;
			}

			public String getGoodsName()
			{
				return goodsName;
			}

			public void setGoodsName(String goodsName)
			{
				this.goodsName = goodsName;
			}

			public BigDecimal getNum()
			{
				return num;
			}

			public void setNum(BigDecimal num)
			{
				this.num = num;
			}

			public BigDecimal getFixedPrice()
			{
				return fixedPrice;
			}

			public void setFixedPrice(Boolean fixedPrice)
			{
				isFixedPrice = fixedPrice;
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

			public void setFixedPrice(BigDecimal fixedPrice)
			{
				this.fixedPrice = fixedPrice;
			}

			public BigDecimal getRatio()
			{
				return ratio;
			}

			public void setRatio(BigDecimal ratio)
			{
				this.ratio = ratio;
			}
		}
	}

}
