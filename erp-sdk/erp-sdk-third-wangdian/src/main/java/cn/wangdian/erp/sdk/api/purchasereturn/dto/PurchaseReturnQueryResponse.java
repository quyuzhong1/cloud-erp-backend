package cn.wangdian.erp.sdk.api.purchasereturn.dto;

import java.math.BigDecimal;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PurchaseReturnQueryResponse
{

	@SerializedName("total_count")
	private int totalCount;

	@SerializedName("order")
	private List<OrderItem> orders;

	public static class OrderItem{

		@SerializedName("order_no")
		private String orderNo;

		@SerializedName("details_list")
		private List<DetailItem> details;

		@SerializedName("consign_time")
		private Long consignTime;

		@SerializedName("post_fee")
		private BigDecimal postFee;

		@SerializedName("receiver_city")
		private String receiverCity;

		@SerializedName("goods_type_count")
		private Integer goodsTypeCount;

		@SerializedName("remark")
		private String remark;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("src_order_no")
		private String srcOrderNo;

		@SerializedName("warehouse_no")
		private String warehouseNo;

		@SerializedName("receiver_telno")
		private String receiverTelno;

		@SerializedName("receiver_name")
		private String receiverName;

		@SerializedName("provider_name")
		private String providerName;

		@SerializedName("provider_no")
		private String providerNo;

		@SerializedName("seq_no")
		private Integer seqNo;

		@SerializedName("create_time")
		private Long createTime;

		@SerializedName("receiver_province")
		private String receiverProvince;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("goods_total_amount")
		private BigDecimal goodsTotalAmount;

		@SerializedName("receiver_district")
		private String receiverDistrict;

		@SerializedName("operator_name")
		private String operatorName;

		@SerializedName("goods_total_cost")
		private String goodsTotalCost;

		@SerializedName("last_load_purchase_no")
		private String lastLoadPurchaseNo;

		@SerializedName("receiver_address")
		private String receiverAddress;

		@SerializedName("status")
		private Byte status;

		public String getOrderNo()
		{
			return orderNo;
		}

		public void setOrderNo(String orderNo)
		{
			this.orderNo = orderNo;
		}

		public List<DetailItem> getDetails()
		{
			return details;
		}

		public void setDetails(List<DetailItem> details)
		{
			this.details = details;
		}

		public Long getConsignTime()
		{
			return consignTime;
		}

		public void setConsignTime(Long consignTime)
		{
			this.consignTime = consignTime;
		}

		public BigDecimal getPostFee()
		{
			return postFee;
		}

		public void setPostFee(BigDecimal postFee)
		{
			this.postFee = postFee;
		}

		public String getReceiverCity()
		{
			return receiverCity;
		}

		public void setReceiverCity(String receiverCity)
		{
			this.receiverCity = receiverCity;
		}

		public Integer getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(Integer goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Integer getStockoutId()
		{
			return stockoutId;
		}

		public void setStockoutId(Integer stockoutId)
		{
			this.stockoutId = stockoutId;
		}

		public String getSrcOrderNo()
		{
			return srcOrderNo;
		}

		public void setSrcOrderNo(String srcOrderNo)
		{
			this.srcOrderNo = srcOrderNo;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public String getReceiverTelno()
		{
			return receiverTelno;
		}

		public void setReceiverTelno(String receiverTelno)
		{
			this.receiverTelno = receiverTelno;
		}

		public String getReceiverName()
		{
			return receiverName;
		}

		public void setReceiverName(String receiverName)
		{
			this.receiverName = receiverName;
		}

		public String getProviderName()
		{
			return providerName;
		}

		public void setProviderName(String providerName)
		{
			this.providerName = providerName;
		}

		public String getProviderNo()
		{
			return providerNo;
		}

		public void setProviderNo(String providerNo)
		{
			this.providerNo = providerNo;
		}

		public Integer getSeqNo()
		{
			return seqNo;
		}

		public void setSeqNo(Integer seqNo)
		{
			this.seqNo = seqNo;
		}

		public Long getCreateTime()
		{
			return createTime;
		}

		public void setCreateTime(Long createTime)
		{
			this.createTime = createTime;
		}

		public String getReceiverProvince()
		{
			return receiverProvince;
		}

		public void setReceiverProvince(String receiverProvince)
		{
			this.receiverProvince = receiverProvince;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public BigDecimal getGoodsTotalAmount()
		{
			return goodsTotalAmount;
		}

		public void setGoodsTotalAmount(BigDecimal goodsTotalAmount)
		{
			this.goodsTotalAmount = goodsTotalAmount;
		}

		public String getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(String receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
		}

		public String getOperatorName()
		{
			return operatorName;
		}

		public void setOperatorName(String operatorName)
		{
			this.operatorName = operatorName;
		}

		public String getGoodsTotalCost()
		{
			return goodsTotalCost;
		}

		public void setGoodsTotalCost(String goodsTotalCost)
		{
			this.goodsTotalCost = goodsTotalCost;
		}

		public String getLastLoadPurchaseNo()
		{
			return lastLoadPurchaseNo;
		}

		public void setLastLoadPurchaseNo(String lastLoadPurchaseNo)
		{
			this.lastLoadPurchaseNo = lastLoadPurchaseNo;
		}

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}
	}

	public static class DetailItem
	{
		@SerializedName("goods_name")
		private String goodsName;

		@SerializedName("spec_code")
		private String specCode;

		@SerializedName("sell_pr ice")
		private BigDecimal sellPrIce;

		@SerializedName("batch_no")
		private String batchNo;

		@SerializedName("brand_no")
		private String brandNo;

		@SerializedName("goods_no")
		private String goodsNo;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("remark")
		private String remark;

		@SerializedName("brand_name")
		private String brandName;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("rec_id")
		private Long recId;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("goods_ type")
		private Integer goodsType;

		@SerializedName("expire_date")
		private String expireDate;

		@SerializedName("total_amount")
		private BigDecimal totalAmount;

		@SerializedName("spec_name")
		private String specName;

		@SerializedName("cost_price")
		private BigDecimal costPrice;

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

		public BigDecimal getSellPrIce()
		{
			return sellPrIce;
		}

		public void setSellPrIce(BigDecimal sellPrIce)
		{
			this.sellPrIce = sellPrIce;
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

		public Long getRecId()
		{
			return recId;
		}

		public void setRecId(Long recId)
		{
			this.recId = recId;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public Integer getStockoutId()
		{
			return stockoutId;
		}

		public void setStockoutId(Integer stockoutId)
		{
			this.stockoutId = stockoutId;
		}

		public Integer getGoodsType()
		{
			return goodsType;
		}

		public void setGoodsType(Integer goodsType)
		{
			this.goodsType = goodsType;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public BigDecimal getCostPrice()
		{
			return costPrice;
		}

		public void setCostPrice(BigDecimal costPrice)
		{
			this.costPrice = costPrice;
		}
	}

	public int getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(int totalCount)
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