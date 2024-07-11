package com.sdk.wangdian.sdk.api.wms.stockout.dto;

import java.math.BigDecimal;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class StockoutOtherQueryResponse
{
	@SerializedName("total_count")
	private Integer totalCount;

	public void setTotalCount(String totalCount) {
		this.totalCount = StringUtils.isBlank(totalCount)? 0 : Integer.parseInt(totalCount);
	}

	@SerializedName("order")
	private List<OrderItem> order;

	@Data
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
		private BigDecimal goodsCount;

		@SerializedName("rec_id")
		private String recId;


		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("stockout_id")
		private String stockoutId;


		@SerializedName("defect")
		private Boolean defect;

		@SerializedName("expire_date")
		private String expireDate;

		@SerializedName("sell_price")
		private BigDecimal sellPrice;

		public void setSellPrice(String sellPrice) {
			if(sellPrice == null || StringUtils.isBlank(sellPrice)){
				this.sellPrice = new BigDecimal(0);
			}else {
				this.sellPrice = BigDecimal.valueOf(Long.parseLong(sellPrice));
			}

		}

		@SerializedName("total_amount")
		private BigDecimal totalAmount;

		@SerializedName("goods_type")
		private Integer goodsType;

		@SerializedName("spec_name")
		private String specName;

		@SerializedName("cost_price")
		private BigDecimal costPrice;

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

	@Data
	public static class OrderItem
	{
		@SerializedName("order_no")
		private String orderNo;

		@SerializedName("reason")
		private String reason;

		@SerializedName("post_fee")
		private BigDecimal postFee;

		@SerializedName("receiver_city")
		private String receiverCity;

		@SerializedName("detail_list")
		private List<DetailListItem> detailList;

		@SerializedName("status")
		private Integer status;

		public void setStatus(String status) {
			this.status = StringUtils.isBlank(status) ? 0 : Integer.parseInt(status);
		}

		@SerializedName("remark")
		private String remark;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("stockout_id")
		private String stockoutId;



		@SerializedName("receiver_province")
		private String receiverProvince;


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
		private String receiverCountry;

		@SerializedName("order_type")
		private Integer orderType;

		public void setOrderType(String orderType) {
			this.orderType = StringUtils.isBlank(orderType) ? 0 : Integer.parseInt(orderType);
		}

		@SerializedName("consign_time")
		private String consignTime;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("receiver_district")
		private String receiverDistrict;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("goods_total_amount")
		private BigDecimal goodsTotalAmount;

		public void setGoodsTotalAmount(String goodsTotalAmount) {
			this.goodsTotalAmount = StringUtils.isBlank(goodsTotalAmount) ? new BigDecimal(0) : BigDecimal.valueOf(Long.parseLong(goodsTotalAmount));
		}

		@SerializedName("receiver_mobile")
		private String receiverMobile;

		@SerializedName("operator_name")
		private String operatorName;

		@SerializedName("goods_total_cost")
		private String goodsTotalCost;

		@SerializedName("receiver_address")
		private String receiverAddress;

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