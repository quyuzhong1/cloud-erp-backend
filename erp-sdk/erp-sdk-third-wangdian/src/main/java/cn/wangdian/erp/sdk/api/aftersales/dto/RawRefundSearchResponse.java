package cn.wangdian.erp.sdk.api.aftersales.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class RawRefundSearchResponse
{
	@SerializedName("total_count")
	private Integer totalCount;

	@SerializedName("order")
	private List<OrderItem> orders;

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

	static class OrderItem
	{
		@SerializedName("logistics_name")
		private String logisticsName;

		@SerializedName("reason")
		private String reason;

		@SerializedName("refund_no")
		private String refundNo;

		@SerializedName("num")
		private BigDecimal num;

		@SerializedName("goods_no")
		private String goodsNo;

		@SerializedName("remark")
		private String remark;

		@SerializedName("sub_platform_id")
		private Short subPlatformId;

		@SerializedName("oid")
		private String oid;

		@SerializedName("type")
		private Byte type;

		@SerializedName("title")
		private String title;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("tid")
		private String tid;

		@SerializedName("current_phase_timeout")
		private String currentPhaseTimeout;

		@SerializedName("pay_no")
		private String payNo;

		@SerializedName("price")
		private BigDecimal price;

		@SerializedName("spec_id")
		private String specId;

		@SerializedName("refund_amount")
		private BigDecimal refundAmount;

		@SerializedName("modified")
		private String modified;

		@SerializedName("shop_no")
		private String shopNo;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("is_aftersale")
		private Boolean isAftersale;

		@SerializedName("created")
		private String created;

		@SerializedName("goods_id")
		private String goodsId;

		@SerializedName("shop_name")
		private String shopName;

		@SerializedName("refund_id")
		private Integer refundId;

		@SerializedName("reason_id")
		private Integer reasonId;

		@SerializedName("shop_id")
		private Integer shopId;

		@SerializedName("actual_refund_amount")
		private BigDecimal actualRefundAmount;

		@SerializedName("total_amount")
		private BigDecimal totalAmount;

		@SerializedName("refund_time")
		private String refundTime;

		@SerializedName("process_status")
		private Byte processStatus;

		@SerializedName("platform_id")
		private Short platformId;

		@SerializedName("status")
		private Byte status;

		@SerializedName("detail_list")
		private List<Detail> detailList;

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}

		public String getRefundNo()
		{
			return refundNo;
		}

		public void setRefundNo(String refundNo)
		{
			this.refundNo = refundNo;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Short getSubPlatformId()
		{
			return subPlatformId;
		}

		public void setSubPlatformId(Short subPlatformId)
		{
			this.subPlatformId = subPlatformId;
		}

		public String getOid()
		{
			return oid;
		}

		public void setOid(String oid)
		{
			this.oid = oid;
		}

		public Byte getType()
		{
			return type;
		}

		public void setType(Byte type)
		{
			this.type = type;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public String getCurrentPhaseTimeout()
		{
			return currentPhaseTimeout;
		}

		public void setCurrentPhaseTimeout(String currentPhaseTimeout)
		{
			this.currentPhaseTimeout = currentPhaseTimeout;
		}

		public String getPayNo()
		{
			return payNo;
		}

		public void setPayNo(String payNo)
		{
			this.payNo = payNo;
		}

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
		}

		public String getSpecId()
		{
			return specId;
		}

		public void setSpecId(String specId)
		{
			this.specId = specId;
		}

		public BigDecimal getRefundAmount()
		{
			return refundAmount;
		}

		public void setRefundAmount(BigDecimal refundAmount)
		{
			this.refundAmount = refundAmount;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public Boolean getAftersale()
		{
			return isAftersale;
		}

		public void setAftersale(Boolean aftersale)
		{
			isAftersale = aftersale;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(String goodsId)
		{
			this.goodsId = goodsId;
		}

		public String getShopName()
		{
			return shopName;
		}

		public void setShopName(String shopName)
		{
			this.shopName = shopName;
		}

		public Integer getRefundId()
		{
			return refundId;
		}

		public void setRefundId(Integer refundId)
		{
			this.refundId = refundId;
		}

		public Integer getReasonId()
		{
			return reasonId;
		}

		public void setReasonId(Integer reasonId)
		{
			this.reasonId = reasonId;
		}

		public Integer getShopId()
		{
			return shopId;
		}

		public void setShopId(Integer shopId)
		{
			this.shopId = shopId;
		}

		public BigDecimal getActualRefundAmount()
		{
			return actualRefundAmount;
		}

		public void setActualRefundAmount(BigDecimal actualRefundAmount)
		{
			this.actualRefundAmount = actualRefundAmount;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
		}

		public String getRefundTime()
		{
			return refundTime;
		}

		public void setRefundTime(String refundTime)
		{
			this.refundTime = refundTime;
		}

		public Byte getProcessStatus()
		{
			return processStatus;
		}

		public void setProcessStatus(Byte processStatus)
		{
			this.processStatus = processStatus;
		}

		public Short getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Short platformId)
		{
			this.platformId = platformId;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public List<Detail> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<Detail> detail)
		{
			this.detailList = detail;
		}

		public static class Detail
		{
			@SerializedName("type") private Integer type;
			@SerializedName("refund_id") private Integer refundId;
			@SerializedName("oid") private String oid;
			@SerializedName("price") private BigDecimal price;
			@SerializedName("platform_id") private Integer platformId;
			@SerializedName("refund_no") private String refundNo;
			@SerializedName("num") private BigDecimal num;
			@SerializedName("remark") private String remark;
			@SerializedName("total_amount") private BigDecimal totalAmount;
			@SerializedName("tid") private String tid;
			@SerializedName("status") private Integer status;
			@SerializedName("goods_no") private String goodsNo;
			@SerializedName("spec_no") private String specNo;
			@SerializedName("goods_id") private String goodsId;
			@SerializedName("spec_id") private String specId;
			@SerializedName("modified_date") private String modifiedDate;
			@SerializedName("created_date") private String createdDate;
			@SerializedName("goods_name") private String goodsName;
			@SerializedName("spec_name") private String specName;

			public Integer getType()
			{
				return type;
			}

			public void setType(Integer type)
			{
				this.type = type;
			}

			public Integer getRefundId()
			{
				return refundId;
			}

			public void setRefundId(Integer refundId)
			{
				this.refundId = refundId;
			}

			public String getOid()
			{
				return oid;
			}

			public void setOid(String oid)
			{
				this.oid = oid;
			}

			public BigDecimal getPrice()
			{
				return price;
			}

			public void setPrice(BigDecimal price)
			{
				this.price = price;
			}

			public Integer getPlatformId()
			{
				return platformId;
			}

			public void setPlatformId(Integer platformId)
			{
				this.platformId = platformId;
			}

			public String getRefundNo()
			{
				return refundNo;
			}

			public void setRefundNo(String refundNo)
			{
				this.refundNo = refundNo;
			}

			public BigDecimal getNum()
			{
				return num;
			}

			public void setNum(BigDecimal num)
			{
				this.num = num;
			}

			public String getRemark()
			{
				return remark;
			}

			public void setRemark(String remark)
			{
				this.remark = remark;
			}

			public BigDecimal getTotalAmount()
			{
				return totalAmount;
			}

			public void setTotalAmount(BigDecimal totalAmount)
			{
				this.totalAmount = totalAmount;
			}

			public String getTid()
			{
				return tid;
			}

			public void setTid(String tid)
			{
				this.tid = tid;
			}

			public Integer getStatus()
			{
				return status;
			}

			public void setStatus(Integer status)
			{
				this.status = status;
			}

			public String getGoodsNo()
			{
				return goodsNo;
			}

			public void setGoodsNo(String goodsNo)
			{
				this.goodsNo = goodsNo;
			}

			public String getSpecNo()
			{
				return specNo;
			}

			public void setSpecNo(String specNo)
			{
				this.specNo = specNo;
			}

			public String getGoodsId()
			{
				return goodsId;
			}

			public void setGoodsId(String goodsId)
			{
				this.goodsId = goodsId;
			}

			public String getSpecId()
			{
				return specId;
			}

			public void setSpecId(String specId)
			{
				this.specId = specId;
			}

			public String getModifiedDate()
			{
				return modifiedDate;
			}

			public void setModifiedDate(String modifiedDate)
			{
				this.modifiedDate = modifiedDate;
			}

			public String getCreatedDate()
			{
				return createdDate;
			}

			public void setCreatedDate(String createdDate)
			{
				this.createdDate = createdDate;
			}

			public String getGoodsName()
			{
				return goodsName;
			}

			public void setGoodsName(String goodsName)
			{
				this.goodsName = goodsName;
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
}
