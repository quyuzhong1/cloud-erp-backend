package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class GoodsSearchResponse
{

	/*
	  { "status": 0, "data": { "goods_list": [{ "goods_name":
	  "阳台桌椅真藤椅茶几三件套五件套组合庭院户外花园客厅阳台藤椅子", "goods_no": "daba3", "origin": "",
	  "goods_modified": 1559295864000, "remark": "", "brand_name": "牛牛2",
	  "aux_unit_name": "无", "prop6": "", "prop5": "", "prop4": "", "flag_name":
	  "无", "prop3": "", "prop2": "", "unit_name": "无", "prop1": "", "pinyin":
	  "", "spec_list": [{ "spec_no": "daba3", "spec_code": "藤椅*1", "barcode":
	  "daba3", "spec_name": "默认规格", "lowest_price": 0.0000, "retail_price":
	  30.0000, "wholesale_price": 0.0000, "member_price": 0.0000,
	  "market_price": 0.0000, "validity_days": 0, "sales_days": 0,
	  "receive_days": 0, "weight": 1.2500, "length": 0.0000, "width": 0.0000,
	  "height": 0.0000, "sn_type": 0, "is_lower_cost": false, "tax_rate":
	  0.0000, "wms_process_mask": 72, "large_type": 0, "remark": "",
	  "spec_modified": 1566280838000, "prop1": "", "prop2": "", "prop3": "",
	  "prop4": "444", "prop5": "555", "prop6": "", "img_url":
	  "https://image.suning.cn/uimg/b2c/newcatentries/0070132569-000000000146026238_2.jpg_800w_800h_4e",
	  "is_not_use_air": 0, "spec_unit_name": "无", "spec_aux_unit_name": "无",
	  "goods_label": "陆路禁运" }], "alias": "", "short_name":
	  "阳台桌椅真藤椅茶几三件套五件套组合庭院户外花园客厅阳台藤椅子", "spec_count": 1, "goods_type": 1,
	  "class_name": "test0104" }], "total_count": 1 } }
	 */

	@SerializedName("total_count")
	private Integer total;
	@SerializedName("goods_list")
	private List<GoodsSearchGoodsDto> goodsInfos;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<GoodsSearchGoodsDto> getGoodsInfos()
	{
		return goodsInfos;
	}

	public void setGoodsInfos(List<GoodsSearchGoodsDto> goodsInfos)
	{
		this.goodsInfos = goodsInfos;
	}

	public static class GoodsSearchGoodsDto
	{

		private Integer goodsId;
		private String goodsName;
		private String goodsNo;
		private String origin;
		private String goodsModified;
		private String goodsCreated;
		private String remark;
		private Integer brandId;
		private String brandName;
		private String auxUnitName;
		private String prop6;
		private String prop5;
		private String prop4;
		private String flagName;
		private String prop3;
		private String prop2;
		private String unitName;
		private String prop1;
		private String pinyin;
		private String alias;
		private String shortName;
		private Integer specCount;
		private Byte goodsType;
		private String className;
		private Integer classId;
		private Integer deleted;
		@SerializedName("spec_list")
		private List<GoodsSearchSpecDto> specDtos;

		public Integer getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(Integer goodsId)
		{
			this.goodsId = goodsId;
		}

		public String getGoodsCreated()
		{
			return goodsCreated;
		}

		public void setGoodsCreated(String goodsCreated)
		{
			this.goodsCreated = goodsCreated;
		}

		public Integer getBrandId()
		{
			return brandId;
		}

		public void setBrandId(Integer brandId)
		{
			this.brandId = brandId;
		}

		public Integer getClassId()
		{
			return classId;
		}

		public void setClassId(Integer classId)
		{
			this.classId = classId;
		}

		public Integer getDeleted()
		{
			return deleted;
		}

		public void setDeleted(Integer deleted)
		{
			this.deleted = deleted;
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

		public String getOrigin()
		{
			return origin;
		}

		public void setOrigin(String origin)
		{
			this.origin = origin;
		}

		public String getGoodsModified()
		{
			return new Date(Long.parseLong(goodsModified)).toString();
		}

		public void setGoodsModified(String goodsModified)
		{
			this.goodsModified = goodsModified;
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

		public String getAuxUnitName()
		{
			return auxUnitName;
		}

		public void setAuxUnitName(String auxUnitName)
		{
			this.auxUnitName = auxUnitName;
		}

		public String getProp6()
		{
			return prop6;
		}

		public void setProp6(String prop6)
		{
			this.prop6 = prop6;
		}

		public String getProp5()
		{
			return prop5;
		}

		public void setProp5(String prop5)
		{
			this.prop5 = prop5;
		}

		public String getProp4()
		{
			return prop4;
		}

		public void setProp4(String prop4)
		{
			this.prop4 = prop4;
		}

		public String getFlagName()
		{
			return flagName;
		}

		public void setFlagName(String flagName)
		{
			this.flagName = flagName;
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

		public String getUnitName()
		{
			return unitName;
		}

		public void setUnitName(String unitName)
		{
			this.unitName = unitName;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getPinyin()
		{
			return pinyin;
		}

		public void setPinyin(String pinyin)
		{
			this.pinyin = pinyin;
		}

		public String getAlias()
		{
			return alias;
		}

		public void setAlias(String alias)
		{
			this.alias = alias;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public Integer getSpecCount()
		{
			return specCount;
		}

		public void setSpecCount(Integer specCount)
		{
			this.specCount = specCount;
		}

		public Byte getGoodsType()
		{
			return goodsType;
		}

		public void setGoodsType(Byte goodsType)
		{
			this.goodsType = goodsType;
		}

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public List<GoodsSearchSpecDto> getSpecDtos()
		{
			return specDtos;
		}

		public void setSpecDtos(List<GoodsSearchSpecDto> specDtos)
		{
			this.specDtos = specDtos;
		}
	}

	public static class GoodsSearchSpecDto
	{

		private Integer goodsId;
		private Integer specId;
		private String specNo;
		private String specCode;
		private String barcode;
		private String specName;
		private BigDecimal lowestPrice;
		private BigDecimal retailPrice;
		private BigDecimal wholesalePrice;
		private BigDecimal memberPrice;
		private BigDecimal marketPrice;
		private BigDecimal validityDays;
		private BigDecimal salesDays;
		private BigDecimal receiveDays;
		private BigDecimal weight;
		private BigDecimal length;
		private BigDecimal width;
		private BigDecimal height;
		private BigDecimal snType;
		@SerializedName("is_lower_cost")
		private Boolean lowerCost;
		@SerializedName("is_not_use_air")
		private Boolean notUseAir;
		private BigDecimal taxRate;
		private Byte wmsProcessMask;
		private Byte largeType;
		private String remark;
		private String specModified;
		private String specCreated;
		private String prop1;
		private String prop2;
		private String prop3;
		private String prop4;
		private String prop5;
		private String prop6;
		private String imgUrl;
		private String specUnitName;
		private String specAuxUnitName;
		private String goodsLabel;
		private Integer deleted;
		private BigDecimal customPrice1;
		private BigDecimal customPrice2;
		@SerializedName("barcode_list")
		private List<GoodsSearchBarcodeDto> barcodeList;

		public Integer getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(Integer goodsId)
		{
			this.goodsId = goodsId;
		}

		public Integer getSpecId()
		{
			return specId;
		}

		public void setSpecId(Integer specId)
		{
			this.specId = specId;
		}

		public String getSpecCreated()
		{
			return specCreated;
		}

		public void setSpecCreated(String specCreated)
		{
			this.specCreated = specCreated;
		}

		public Integer getDeleted()
		{
			return deleted;
		}

		public void setDeleted(Integer deleted)
		{
			this.deleted = deleted;
		}

		public BigDecimal getCustomPrice1()
		{
			return customPrice1;
		}

		public void setCustomPrice1(BigDecimal customPrice1)
		{
			this.customPrice1 = customPrice1;
		}

		public BigDecimal getCustomPrice2()
		{
			return customPrice2;
		}

		public void setCustomPrice2(BigDecimal customPrice2)
		{
			this.customPrice2 = customPrice2;
		}

		public List<GoodsSearchBarcodeDto> getBarcodeList()
		{
			return barcodeList;
		}

		public void setBarcodeList(List<GoodsSearchBarcodeDto> barcodeList)
		{
			this.barcodeList = barcodeList;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
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

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public BigDecimal getLowestPrice()
		{
			return lowestPrice;
		}

		public void setLowestPrice(BigDecimal lowestPrice)
		{
			this.lowestPrice = lowestPrice;
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

		public BigDecimal getValidityDays()
		{
			return validityDays;
		}

		public void setValidityDays(BigDecimal validityDays)
		{
			this.validityDays = validityDays;
		}

		public BigDecimal getSalesDays()
		{
			return salesDays;
		}

		public void setSalesDays(BigDecimal salesDays)
		{
			this.salesDays = salesDays;
		}

		public BigDecimal getReceiveDays()
		{
			return receiveDays;
		}

		public void setReceiveDays(BigDecimal receiveDays)
		{
			this.receiveDays = receiveDays;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public BigDecimal getLength()
		{
			return length;
		}

		public void setLength(BigDecimal length)
		{
			this.length = length;
		}

		public BigDecimal getWidth()
		{
			return width;
		}

		public void setWidth(BigDecimal width)
		{
			this.width = width;
		}

		public BigDecimal getHeight()
		{
			return height;
		}

		public void setHeight(BigDecimal height)
		{
			this.height = height;
		}

		public BigDecimal getSnType()
		{
			return snType;
		}

		public void setSnType(BigDecimal snType)
		{
			this.snType = snType;
		}

		public Boolean getLowerCost()
		{
			return lowerCost;
		}

		public void setLowerCost(Boolean lowerCost)
		{
			this.lowerCost = lowerCost;
		}

		public Boolean getNotUseAir()
		{
			return notUseAir;
		}

		public void setNotUseAir(Boolean notUseAir)
		{
			this.notUseAir = notUseAir;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}

		public Byte getWmsProcessMask()
		{
			return wmsProcessMask;
		}

		public void setWmsProcessMask(Byte wmsProcessMask)
		{
			this.wmsProcessMask = wmsProcessMask;
		}

		public Byte getLargeType()
		{
			return largeType;
		}

		public void setLargeType(Byte largeType)
		{
			this.largeType = largeType;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getSpecModified()
		{
			return new Date(Long.parseLong(specModified)).toString();
		}

		public void setSpecModified(String specModified)
		{
			this.specModified = specModified;
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

		public String getProp5()
		{
			return prop5;
		}

		public void setProp5(String prop5)
		{
			this.prop5 = prop5;
		}

		public String getProp6()
		{
			return prop6;
		}

		public void setProp6(String prop6)
		{
			this.prop6 = prop6;
		}

		public String getImgUrl()
		{
			return imgUrl;
		}

		public void setImgUrl(String imgUrl)
		{
			this.imgUrl = imgUrl;
		}

		public String getSpecUnitName()
		{
			return specUnitName;
		}

		public void setSpecUnitName(String specUnitName)
		{
			this.specUnitName = specUnitName;
		}

		public String getSpecAuxUnitName()
		{
			return specAuxUnitName;
		}

		public void setSpecAuxUnitName(String specAuxUnitName)
		{
			this.specAuxUnitName = specAuxUnitName;
		}

		public String getGoodsLabel()
		{
			return goodsLabel;
		}

		public void setGoodsLabel(String goodsLabel)
		{
			this.goodsLabel = goodsLabel;
		}
	}

	public static class GoodsSearchBarcodeDto
	{
		private String barcode ;
		private Integer type ;
		@SerializedName("is_master")
		private Boolean master ;
		private String modified ;

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public Integer getType()
		{
			return type;
		}

		public void setType(Integer type)
		{
			this.type = type;
		}

		public Boolean getMaster()
		{
			return master;
		}

		public void setMaster(Boolean master)
		{
			this.master = master;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}
	}
}
