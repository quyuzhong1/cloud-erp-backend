package cn.wangdian.erp.sdk.api.purchaseOrder.dto;

import java.math.BigDecimal;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PurchaseOrderQueryResponse
{
	@SerializedName("total_count")
	private Integer totalCount;

	@SerializedName("order")
	private List<OrderItem> orders;

	public static class OrderItem
	{
		@SerializedName("logistics_name")
		private String logisticsName;

		@SerializedName("purchase_no")
		private String purchaseNo;

		@SerializedName("goods_stockin_count")
		private BigDecimal goodsStockinCount;

		@SerializedName("post_fee")
		private BigDecimal postFee;

		@SerializedName("detail_list")
		private List<DetailItem> details;

		@SerializedName("goods_type_count")
		private Integer goodsTypeCount;

		@SerializedName("check_operator_name")
		private String checkOperatorName;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("stockin_status")
		private Byte stockinStatus;

		@SerializedName("flag_name")
		private String flagName;

		@SerializedName("tax_fee")
		private BigDecimal taxFee;

		@SerializedName("total_fee")
		private BigDecimal totalFee;

		@SerializedName("purchaser_name")
		private String purchaserName;

		@SerializedName("modified")
		private Long modified;

		@SerializedName("pay_type")
		private Byte payType;

		@SerializedName("remar k")
		private String remarK;

		@SerializedName("provider_name")
		private String providerName;

		@SerializedName("other_fee")
		private BigDecimal otherFee;

		@SerializedName("facheck_operator_name")
		private String facheckOperatorName;

		@SerializedName("created")
		private Long created;

		@SerializedName("settle_status")
		private Byte settleStatus;

		@SerializedName("postfee_pay_type")
		private Byte postfeePayType;

		@SerializedName("goods_arrive_count")
		private BigDecimal goodsArriveCount;

		@SerializedName("warehouse_no")
		private String warehouseNo;

		@SerializedName("warehouse_name")
		private String warehouseName;

		@SerializedName("expect_arrive_time")
		private String expectArriveTime;

		@SerializedName("creator_name")
		private String creatorName;

		@SerializedName("goods_fe e")
		private BigDecimal goodsFeE;

		@SerializedName("status")
		private Byte status;

		@SerializedName("warehouse_id")
		private Integer warehouseId;

		@SerializedName("check_time")
		private String checkTime;

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getPurchaseNo()
		{
			return purchaseNo;
		}

		public void setPurchaseNo(String purchaseNo)
		{
			this.purchaseNo = purchaseNo;
		}

		public BigDecimal getGoodsStockinCount()
		{
			return goodsStockinCount;
		}

		public void setGoodsStockinCount(BigDecimal goodsStockinCount)
		{
			this.goodsStockinCount = goodsStockinCount;
		}

		public BigDecimal getPostFee()
		{
			return postFee;
		}

		public void setPostFee(BigDecimal postFee)
		{
			this.postFee = postFee;
		}

		public List<DetailItem> getDetails()
		{
			return details;
		}

		public void setDetails(List<DetailItem> details)
		{
			this.details = details;
		}

		public Integer getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(Integer goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
		}

		public String getCheckOperatorName()
		{
			return checkOperatorName;
		}

		public void setCheckOperatorName(String checkOperatorName)
		{
			this.checkOperatorName = checkOperatorName;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Byte getStockinStatus()
		{
			return stockinStatus;
		}

		public void setStockinStatus(Byte stockinStatus)
		{
			this.stockinStatus = stockinStatus;
		}

		public String getFlagName()
		{
			return flagName;
		}

		public void setFlagName(String flagName)
		{
			this.flagName = flagName;
		}

		public BigDecimal getTaxFee()
		{
			return taxFee;
		}

		public void setTaxFee(BigDecimal taxFee)
		{
			this.taxFee = taxFee;
		}

		public BigDecimal getTotalFee()
		{
			return totalFee;
		}

		public void setTotalFee(BigDecimal totalFee)
		{
			this.totalFee = totalFee;
		}

		public String getPurchaserName()
		{
			return purchaserName;
		}

		public void setPurchaserName(String purchaserName)
		{
			this.purchaserName = purchaserName;
		}

		public Long getModified()
		{
			return modified;
		}

		public void setModified(Long modified)
		{
			this.modified = modified;
		}

		public Byte getPayType()
		{
			return payType;
		}

		public void setPayType(Byte payType)
		{
			this.payType = payType;
		}

		public String getRemarK()
		{
			return remarK;
		}

		public void setRemarK(String remarK)
		{
			this.remarK = remarK;
		}

		public String getProviderName()
		{
			return providerName;
		}

		public void setProviderName(String providerName)
		{
			this.providerName = providerName;
		}

		public BigDecimal getOtherFee()
		{
			return otherFee;
		}

		public void setOtherFee(BigDecimal otherFee)
		{
			this.otherFee = otherFee;
		}

		public String getFacheckOperatorName()
		{
			return facheckOperatorName;
		}

		public void setFacheckOperatorName(String facheckOperatorName)
		{
			this.facheckOperatorName = facheckOperatorName;
		}

		public Long getCreated()
		{
			return created;
		}

		public void setCreated(Long created)
		{
			this.created = created;
		}

		public Byte getSettleStatus()
		{
			return settleStatus;
		}

		public void setSettleStatus(Byte settleStatus)
		{
			this.settleStatus = settleStatus;
		}

		public Byte getPostfeePayType()
		{
			return postfeePayType;
		}

		public void setPostfeePayType(Byte postfeePayType)
		{
			this.postfeePayType = postfeePayType;
		}

		public BigDecimal getGoodsArriveCount()
		{
			return goodsArriveCount;
		}

		public void setGoodsArriveCount(BigDecimal goodsArriveCount)
		{
			this.goodsArriveCount = goodsArriveCount;
		}

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
		}

		public String getExpectArriveTime()
		{
			return expectArriveTime;
		}

		public void setExpectArriveTime(String expectArriveTime)
		{
			this.expectArriveTime = expectArriveTime;
		}

		public String getCreatorName()
		{
			return creatorName;
		}

		public void setCreatorName(String creatorName)
		{
			this.creatorName = creatorName;
		}

		public BigDecimal getGoodsFeE()
		{
			return goodsFeE;
		}

		public void setGoodsFeE(BigDecimal goodsFeE)
		{
			this.goodsFeE = goodsFeE;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public Integer getWarehouseId()
		{
			return warehouseId;
		}

		public void setWarehouseId(Integer warehouseId)
		{
			this.warehouseId = warehouseId;
		}

		public String getCheckTime()
		{
			return checkTime;
		}

		public void setCheckTime(String checkTime)
		{
			this.checkTime = checkTime;
		}
	}

	public static class DetailItem
	{
		@SerializedName("tax_amount")
		private BigDecimal taxAmount;

		@SerializedName("spec_code")
		private String specCode;

		@SerializedName("goods_no")
		private String goodsNo;

		@SerializedName("num")
		private BigDecimal num;

		@SerializedName("arrive_num")
		private BigDecimal arriveNum;

		@SerializedName("retail_price ")
		private BigDecimal retailPrice;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("tax_price")
		private BigDecimal taxPrice;

		@SerializedName("purchase_unit_name")
		private String purchaseUnitName;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		@SerializedName("re mark")
		private String reMark;

		@SerializedName("base_unit_name")
		private String baseUnitName;

		@SerializedName("price")
		private BigDecimal price;

		@SerializedName("arrive_more_num")
		private BigDecimal arriveMoreNum;

		@SerializedName("gs_prop1")
		private String gsProp1;

		@SerializedName("gs_prop3")
		private String gsProp3;

		@SerializedName("barcode")
		private String barcode;

		@SerializedName("gs_prop2")
		private String gsProp2;

		@SerializedName("gs_prop5")
		private String gsProp5;

		@SerializedName("num2")
		private BigDecimal num2;

		@SerializedName("lack_num")
		private BigDecimal lackNum;

		@SerializedName("gs_prop4")
		private String gsProp4;

		@SerializedName("goods_name")
		private String goodsName;

		@SerializedName("gs_prop6")
		private String gsProp6;

		@SerializedName("amount")
		private BigDecimal amount;

		@SerializedName("aux_amount")
		private BigDecimal auxAmount;

		@SerializedName("stockin_num")
		private BigDecimal stockinNum;

		@SerializedName("stopwait_num")
		private BigDecimal stopwaitNum;

		@SerializedName("unit_ratio")
		private BigDecimal unitRatio;

		@SerializedName("dis_price")
		private BigDecimal disPrice;

		@SerializedName("spec_remark")
		private String specRemark;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("brand_name")
		private String brandName;

		@SerializedName("prop2")
		private String prop2;

		@SerializedName("aux_price")
		private BigDecimal auxPrice;

		@SerializedName("prop1")
		private String prop1;

		@SerializedName("settle_num")
		private BigDecimal settleNum;

		@SerializedName("purchase_price")
		private String purchasePrice;

		@SerializedName("short_name")
		private String shortName;

		@SerializedName("spec_name")
		private String specName;

		@SerializedName("img_ url")
		private String imgUrl;

		@SerializedName("stockin_amount")
		private BigDecimal stockinAmount;

		@SerializedName("provider_goods_no")
		private String providerGoodsNo;

		public BigDecimal getTaxAmount()
		{
			return taxAmount;
		}

		public void setTaxAmount(BigDecimal taxAmount)
		{
			this.taxAmount = taxAmount;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getArriveNum()
		{
			return arriveNum;
		}

		public void setArriveNum(BigDecimal arriveNum)
		{
			this.arriveNum = arriveNum;
		}

		public BigDecimal getRetailPrice()
		{
			return retailPrice;
		}

		public void setRetailPrice(BigDecimal retailPrice)
		{
			this.retailPrice = retailPrice;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getTaxPrice()
		{
			return taxPrice;
		}

		public void setTaxPrice(BigDecimal taxPrice)
		{
			this.taxPrice = taxPrice;
		}

		public String getPurchaseUnitName()
		{
			return purchaseUnitName;
		}

		public void setPurchaseUnitName(String purchaseUnitName)
		{
			this.purchaseUnitName = purchaseUnitName;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}

		public String getReMark()
		{
			return reMark;
		}

		public void setReMark(String reMark)
		{
			this.reMark = reMark;
		}

		public String getBaseUnitName()
		{
			return baseUnitName;
		}

		public void setBaseUnitName(String baseUnitName)
		{
			this.baseUnitName = baseUnitName;
		}

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
		}

		public BigDecimal getArriveMoreNum()
		{
			return arriveMoreNum;
		}

		public void setArriveMoreNum(BigDecimal arriveMoreNum)
		{
			this.arriveMoreNum = arriveMoreNum;
		}

		public String getGsProp1()
		{
			return gsProp1;
		}

		public void setGsProp1(String gsProp1)
		{
			this.gsProp1 = gsProp1;
		}

		public String getGsProp3()
		{
			return gsProp3;
		}

		public void setGsProp3(String gsProp3)
		{
			this.gsProp3 = gsProp3;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getGsProp2()
		{
			return gsProp2;
		}

		public void setGsProp2(String gsProp2)
		{
			this.gsProp2 = gsProp2;
		}

		public String getGsProp5()
		{
			return gsProp5;
		}

		public void setGsProp5(String gsProp5)
		{
			this.gsProp5 = gsProp5;
		}

		public BigDecimal getNum2()
		{
			return num2;
		}

		public void setNum2(BigDecimal num2)
		{
			this.num2 = num2;
		}

		public BigDecimal getLackNum()
		{
			return lackNum;
		}

		public void setLackNum(BigDecimal lackNum)
		{
			this.lackNum = lackNum;
		}

		public String getGsProp4()
		{
			return gsProp4;
		}

		public void setGsProp4(String gsProp4)
		{
			this.gsProp4 = gsProp4;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getGsProp6()
		{
			return gsProp6;
		}

		public void setGsProp6(String gsProp6)
		{
			this.gsProp6 = gsProp6;
		}

		public BigDecimal getAmount()
		{
			return amount;
		}

		public void setAmount(BigDecimal amount)
		{
			this.amount = amount;
		}

		public BigDecimal getAuxAmount()
		{
			return auxAmount;
		}

		public void setAuxAmount(BigDecimal auxAmount)
		{
			this.auxAmount = auxAmount;
		}

		public BigDecimal getStockinNum()
		{
			return stockinNum;
		}

		public void setStockinNum(BigDecimal stockinNum)
		{
			this.stockinNum = stockinNum;
		}

		public BigDecimal getStopwaitNum()
		{
			return stopwaitNum;
		}

		public void setStopwaitNum(BigDecimal stopwaitNum)
		{
			this.stopwaitNum = stopwaitNum;
		}

		public BigDecimal getUnitRatio()
		{
			return unitRatio;
		}

		public void setUnitRatio(BigDecimal unitRatio)
		{
			this.unitRatio = unitRatio;
		}

		public BigDecimal getDisPrice()
		{
			return disPrice;
		}

		public void setDisPrice(BigDecimal disPrice)
		{
			this.disPrice = disPrice;
		}

		public String getSpecRemark()
		{
			return specRemark;
		}

		public void setSpecRemark(String specRemark)
		{
			this.specRemark = specRemark;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public BigDecimal getAuxPrice()
		{
			return auxPrice;
		}

		public void setAuxPrice(BigDecimal auxPrice)
		{
			this.auxPrice = auxPrice;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public BigDecimal getSettleNum()
		{
			return settleNum;
		}

		public void setSettleNum(BigDecimal settleNum)
		{
			this.settleNum = settleNum;
		}

		public String getPurchasePrice()
		{
			return purchasePrice;
		}

		public void setPurchasePrice(String purchasePrice)
		{
			this.purchasePrice = purchasePrice;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getImgUrl()
		{
			return imgUrl;
		}

		public void setImgUrl(String imgUrl)
		{
			this.imgUrl = imgUrl;
		}

		public BigDecimal getStockinAmount()
		{
			return stockinAmount;
		}

		public void setStockinAmount(BigDecimal stockinAmount)
		{
			this.stockinAmount = stockinAmount;
		}

		public String getProviderGoodsNo()
		{
			return providerGoodsNo;
		}

		public void setProviderGoodsNo(String providerGoodsNo)
		{
			this.providerGoodsNo = providerGoodsNo;
		}
	}

	public Integer getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(Integer totalCount)
	{
		this.totalCount = totalCount;
	}

	public List<OrderItem> getOrders()
	{
		return orders;
	}

	public void setOrders(List<OrderItem> orders)
	{
		this.orders = orders;
	}
}