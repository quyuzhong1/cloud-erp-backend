package cn.wangdian.erp.sdk.api.wms.stockout.dto;

import java.math.BigDecimal;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StockoutOtherQueryResponse
{
	@SerializedName("total_count")
	private Integer totalCount;

	@SerializedName("order")
	private List<OrderItem> order;

	public Integer getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(Integer totalCount)
	{
		this.totalCount = totalCount;
	}

	public List<OrderItem> getOrder()
	{
		return order;
	}

	public void setOrder(List<OrderItem> order)
	{
		this.order = order;
	}

	public static class DetailListItem
	{
		@SerializedName("goods_name")
		private String goodsName;

		@SerializedName("spec_code")
		private String specCode;

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
		private Integer goodsCount;

		@SerializedName("rec_id")
		private Integer recId;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("defect")
		private Boolean defect;

		@SerializedName("expire_date")
		private String expireDate;

		@SerializedName("sell_price")
		private BigDecimal sellPrice;

		@SerializedName("total_amount")
		private BigDecimal totalAmount;

		@SerializedName("goods_type")
		private Byte goodsType;

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

		public Integer getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(Integer goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Integer getRecId()
		{
			return recId;
		}

		public void setRecId(Integer recId)
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

		public Boolean getDefect()
		{
			return defect;
		}

		public void setDefect(Boolean defect)
		{
			this.defect = defect;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
		}

		public BigDecimal getSellPrice()
		{
			return sellPrice;
		}

		public void setSellPrice(BigDecimal sellPrice)
		{
			this.sellPrice = sellPrice;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
		}

		public Byte getGoodsType()
		{
			return goodsType;
		}

		public void setGoodsType(Byte goodsType)
		{
			this.goodsType = goodsType;
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

		@Override
		public String toString()
		{
			return "DetailItem{" + "goodsName='" + goodsName + '\'' + ", specCode='" + specCode + '\''
					+ ", brandNo='" + brandNo + '\'' + ", goodsNo='" + goodsNo + '\'' + ", weight=" + weight
					+ ", remark='" + remark + '\'' + ", brandName='" + brandName + '\'' + ", goodsCount=" + goodsCount
					+ ", recId=" + recId + ", specNo='" + specNo + '\'' + ", stockoutId=" + stockoutId + ", defect="
					+ defect + ", expireDate='" + expireDate + '\'' + ", sellPrice=" + sellPrice + ", totalAmount="
					+ totalAmount + ", goodsType=" + goodsType + ", specName='" + specName + '\'' + ", costPrice="
					+ costPrice + '}';
		}
	}

	public static class OrderItem
	{
		@SerializedName("order_no")
		private String orderNo;

		@SerializedName("reason")
		private String reason;

		@SerializedName("post_fee")
		private BigDecimal postFee;

		@SerializedName("receiver_city")
		private Integer receiverCity;

		@SerializedName("detail_list")
		private List<DetailListItem> detailList;

		@SerializedName("status")
		private Integer status;

		@SerializedName("remark")
		private String remark;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("receiver_province")
		private Integer receiverProvince;

		@SerializedName("src_order_no")
		private String srcOrderNo;

		@SerializedName("warehouse_no")
		private String warehouseNo;

		@SerializedName("receiver_telno")
		private String receiverTelno;

		@SerializedName("receiver_zip")
		private String receiverZip;

		@SerializedName("receiver_name")
		private String receiverName;

		@SerializedName("receiver_country")
		private Integer receiverCountry;

		@SerializedName("order_type")
		private Integer orderType;

		@SerializedName("consign_time")
		private Long consignTime;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("receiver_district")
		private BigDecimal receiverDistrict;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("goods_total_amount")
		private BigDecimal goodsTotalAmount;

		@SerializedName("receiver_mobile")
		private String receiverMobile;

		@SerializedName("operator_name")
		private String operatorName;

		@SerializedName("goods_total_cost")
		private String goodsTotalCost;

		@SerializedName("receiver_address")
		private String receiverAddress;

		public String getOrderNo()
		{
			return orderNo;
		}

		public void setOrderNo(String orderNo)
		{
			this.orderNo = orderNo;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}

		public BigDecimal getPostFee()
		{
			return postFee;
		}

		public void setPostFee(BigDecimal postFee)
		{
			this.postFee = postFee;
		}

		public Integer getReceiverCity()
		{
			return receiverCity;
		}

		public void setReceiverCity(Integer receiverCity)
		{
			this.receiverCity = receiverCity;
		}

		public List<DetailListItem> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<DetailListItem> detailList)
		{
			this.detailList = detailList;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
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

		public Integer getReceiverProvince()
		{
			return receiverProvince;
		}

		public void setReceiverProvince(Integer receiverProvince)
		{
			this.receiverProvince = receiverProvince;
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

		public String getReceiverZip()
		{
			return receiverZip;
		}

		public void setReceiverZip(String receiverZip)
		{
			this.receiverZip = receiverZip;
		}

		public String getReceiverName()
		{
			return receiverName;
		}

		public void setReceiverName(String receiverName)
		{
			this.receiverName = receiverName;
		}

		public Integer getReceiverCountry()
		{
			return receiverCountry;
		}

		public void setReceiverCountry(Integer receiverCountry)
		{
			this.receiverCountry = receiverCountry;
		}

		public Integer getOrderType()
		{
			return orderType;
		}

		public void setOrderType(Integer orderType)
		{
			this.orderType = orderType;
		}

		public Long getConsignTime()
		{
			return consignTime;
		}

		public void setConsignTime(Long consignTime)
		{
			this.consignTime = consignTime;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public BigDecimal getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(BigDecimal receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
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

		public String getReceiverMobile()
		{
			return receiverMobile;
		}

		public void setReceiverMobile(String receiverMobile)
		{
			this.receiverMobile = receiverMobile;
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

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		@Override
		public String toString()
		{
			return "OrderItem{" + "orderNo='" + orderNo + '\'' + ", reason='" + reason + '\'' + ", postFee=" + postFee
					+ ", receiverCity=" + receiverCity + ", detailList=" + detailList + ", status=" + status
					+ ", remark='" + remark + '\'' + ", goodsCount=" + goodsCount + ", stockoutId=" + stockoutId
					+ ", receiverProvince=" + receiverProvince + ", srcOrderNo='" + srcOrderNo + '\''
					+ ", warehouseNo='" + warehouseNo + '\'' + ", receiverTelno='" + receiverTelno + '\''
					+ ", receiverZip='" + receiverZip + '\'' + ", receiverName='" + receiverName + '\''
					+ ", receiverCountry=" + receiverCountry + ", orderType=" + orderType + ", consignTime="
					+ consignTime + ", logisticsNo='" + logisticsNo + '\'' + ", receiverDistrict=" + receiverDistrict
					+ ", weight=" + weight + ", goodsTotalAmount=" + goodsTotalAmount + ", receiverMobile='"
					+ receiverMobile + '\'' + ", operatorName='" + operatorName + '\'' + ", goodsTotalCost='"
					+ goodsTotalCost + '\'' + ", receiverAddress='" + receiverAddress + '\'' + '}';
		}
	}

	@Override
	public String toString()
	{
		return "StockoutOtherQueryResponse{" + "total_count = '" + totalCount + '\'' + ",order = '" + order + '\''
				+ "}";
	}
}