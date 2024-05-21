package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class GoodsSuiteUploadRequest
{
	public static class Suite
	{
		public static final byte PRINT_SUITE_ONLY_DETAIL = 0; // 组合装明细
		public static final byte PRINT_SUITE_AND_DETAIL = 1; // 组合装以及明细
		public static final byte PRINT_SUITE_ONLY_MAIN = 2; // 打印组合装

		public static final byte LARGE_TYPE_NONE = 0;		//非大件
		public static final byte LARGE_TYPE_NORMAL = 1;		//普通大件
		public static final byte LARGE_TYPE_EXCLUSIVE = 2;	//独立大件

		@SerializedName("suite_no")
		private String suiteNo;

		@SerializedName("suite_name")
		private String suiteName;

		@SerializedName("short_name")
		private String shortName;

		@SerializedName("barcode")
		private String barcode;

		@SerializedName("brand_name")
		private String brandName;

		@SerializedName("class_name")
		private String className;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("unit_name")
		private String unitName;

		@SerializedName("aux_unit_name")
		private String auxUnitName;

		@SerializedName("prop1")
		private String prop1;

		@SerializedName("prop2")
		private String prop2;

		@SerializedName("prop3")
		private String prop3;

		@SerializedName("prop4")
		private String prop4;

		@SerializedName("retail_price")
		private BigDecimal retailPrice;

		@SerializedName("wholesale_price")
		private BigDecimal wholesalePrice;

		@SerializedName("member_price")
		private BigDecimal memberPrice;

		@SerializedName("market_price")
		private BigDecimal marketPrice;

		@SerializedName("remark")
		private String remark;

		@SerializedName("print_suite_mode")
		private Byte printSuiteMode;

		@SerializedName("goods_label_name")
		private String goodsLabelName;

		@SerializedName("large_type")
		private Byte largeType;

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

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

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

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
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

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getProp3()
		{
			return prop3;
		}

		public void setProp3(String prop3)
		{
			this.prop3 = prop3;
		}

		public String getProp4()
		{
			return prop4;
		}

		public void setProp4(String prop4)
		{
			this.prop4 = prop4;
		}

		public BigDecimal getRetailPrice()
		{
			return retailPrice;
		}

		public void setRetailPrice(BigDecimal retailPrice)
		{
			this.retailPrice = retailPrice;
		}

		public BigDecimal getWholesalePrice()
		{
			return wholesalePrice;
		}

		public void setWholesalePrice(BigDecimal wholesalePrice)
		{
			this.wholesalePrice = wholesalePrice;
		}

		public BigDecimal getMemberPrice()
		{
			return memberPrice;
		}

		public void setMemberPrice(BigDecimal memberPrice)
		{
			this.memberPrice = memberPrice;
		}

		public BigDecimal getMarketPrice()
		{
			return marketPrice;
		}

		public void setMarketPrice(BigDecimal marketPrice)
		{
			this.marketPrice = marketPrice;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Byte getPrintSuiteMode()
		{
			return printSuiteMode;
		}

		public void setPrintSuiteMode(Byte printSuiteMode)
		{
			this.printSuiteMode = printSuiteMode;
		}

		public String getGoodsLabelName()
		{
			return goodsLabelName;
		}

		public void setGoodsLabelName(String goodsLabelName)
		{
			this.goodsLabelName = goodsLabelName;
		}

		public Byte getLargeType()
		{
			return largeType;
		}

		public void setLargeType(Byte largeType)
		{
			this.largeType = largeType;
		}
	}

	public static class Detail
	{
		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("is_fixed_price")
		private boolean fixedPrice;

		@SerializedName("num")
		private BigDecimal num;

		@SerializedName("fixed_price")
		private BigDecimal price = BigDecimal.ZERO;

		@SerializedName("ratio")
		private BigDecimal ratio;

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public boolean isFixedPrice()
		{
			return fixedPrice;
		}

		public void setFixedPrice(boolean fixedPrice)
		{
			this.fixedPrice = fixedPrice;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
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
