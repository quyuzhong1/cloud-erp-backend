package com.sdk.wangdian.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PushSelfRequest
{
	@SerializedName("shop_no")
	private String shopNo;

	public static class RawTrade
	{
		/**
		 * 平台状态 未确认
		 */
		public static final byte TRADE_STATUS_TO_CONFIRM = 10;
		/**
		 * 平台状态 待尾款
		 */
		public static final byte TRADE_STATUS_PART_PAID = 20;
		/**
		 * 平台状态 待发货
		 */
		public static final byte TRADE_STATUS_WAIT_CONSIGN = 30;
		/**
		 * 平台状态 部分发货
		 */
		public static final byte TRADE_STATUS_PART_CONSIGN = 40;
		/**
		 * 平台状态 已发货
		 */
		public static final byte TRADE_STATUS_CONSIGNED = 50;
		/**
		 * 平台状态 已签收
		 */
		public static final byte TRADE_STATUS_SIGNED = 60;
		/**
		 * 平台状态 已完成
		 */
		public static final byte TRADE_STATUS_COMPLETE = 70;
		/**
		 * 平台状态 已退款
		 */
		public static final byte TRADE_STATUS_REFUND = 80;
		/**
		 * 平台状态 已关闭(付款前取消)
		 */
		public static final byte TRADE_STATUS_CLOSED = 90;

		// 付款状态
		public static final byte PAY_STATUS_UNPAY = 0; // 未付款
		public static final byte PAY_STATUS_PART_PAID = 1; // 部分付款
		public static final byte PAY_STATUS_FULL_PAID = 2; // 已付款

		// 退款状态
		public static final byte REFUND_NONE = 0; // 无退款
		public static final byte REFUND_APPLY = 1; // 申请退款
		public static final byte REFUND_PART = 2; // 部分退款
		public static final byte REFUND_FULL = 3; // 全部退款

		/**
		 ** 款到发货
		 */
		public static final byte DELIVERY_TERM_DAP = 1; // 款到发货
		/**
		 * 货到付款
		 */
		public static final byte DELIVERY_TERM_COD = 2; // 货到付款(包含部分货到付款)

		public static final byte PROCESS_STATUS_WAIT_DELIVERY = 10; // 待递交
		public static final byte PROCESS_STATUS_CANCEL = 70; // 已取消

		// 1在线转帐 2现金，3银行转账，4邮局汇款 5预付款 6刷卡
		public static final byte PAY_METHOD_ONLINE = 1;
		public static final byte PAY_METHOD_CASH = 2;
		public static final byte PAY_METHOD_BANK = 3;
		public static final byte PAY_METHOD_REMITTANCE = 4;
		public static final byte PAY_METHOD_ADVANCES = 5;
		public static final byte PAY_METHOD_CARD = 5;

		// 0 不需要，1普通发票，2增值税发票
		public static final byte INVOICE_TYPE_NONE = 0;
		public static final byte INVOICE_TYPE_ORDINARY = 1;
		public static final byte INVOICE_TYPE_VAT = 2;

		@SerializedName("delivery_term")
		private int deliveryTerm = DELIVERY_TERM_DAP;
		@SerializedName("refund_status")
		private byte refundStatus = REFUND_NONE;
		@SerializedName("trade_status")
		private byte tradeStatus = TRADE_STATUS_WAIT_CONSIGN;
		@SerializedName("pay_status")
		private byte payStatus = PAY_STATUS_FULL_PAID;
		@SerializedName("process_status")
		private byte processStatus = PROCESS_STATUS_WAIT_DELIVERY;

		@SerializedName("tid")
		private String tid;

		// !!!
		@SerializedName("is_auto_wms")
		private Boolean autoWms;
		@SerializedName("warehouse_no")
		private String warehouseNo;
		@SerializedName("goods_count")
		private BigDecimal goodsCount = BigDecimal.valueOf(0.0000D);
		@SerializedName("order_count")
		private Integer orderCount = 0;
		@SerializedName("trade_time")
		private String tradeTime;
		@SerializedName("remark_flag")
		private Integer remarkFlag;
		@SerializedName("consign_interval")
		private Integer consignInterval;
		@SerializedName("to_deliver_time")
		private String toDeliverTime;
		@SerializedName("platform_cost")
		private BigDecimal platformCost;
		@SerializedName("logistics_type")
		private Integer logisticsType;
		@SerializedName("end_time")
		private String endTime;
		@SerializedName("pay_time")
		private String payTime;
		@SerializedName("pay_method")
		private Byte payMethod;
		@SerializedName("remark")
		private String remark = "";
		@SerializedName("pay_account")
		private String payAccount = "";
		@SerializedName("pay_id")
		private String payId = "";
		@SerializedName("receiver_zip")
		private String receiverZip = "";
		@SerializedName("receiver_telno")
		private String receiverTelno = "";
		@SerializedName("receiver_name")
		private String receiverName = "";
		@SerializedName("receiver_area")
		private String receiverArea = "";
		@SerializedName("receiver_mobile")
		private String receiverMobile = "";
		@SerializedName("receiver_address")
		private String receiverAddress = "";
		@SerializedName("buyer_nick")
		private String buyerNick = "";
		@SerializedName("buyer_email")
		private String buyerEmail = "";
		@SerializedName("buyer_message")
		private String buyerMessage = "";
		@SerializedName("buyer_area")
		private String buyerArea = "";
		@SerializedName("invoice_type")
		private Byte invoiceType;
		@SerializedName("invoice_content")
		private String invoiceContent = "";
		@SerializedName("invoice_title")
		private String invoiceTitle = "";
		@SerializedName("receivable")
		private BigDecimal receivable = BigDecimal.ZERO; // 应收金额
		@SerializedName("post_amount")
		private BigDecimal postAmount = BigDecimal.ZERO; // 邮费
		@SerializedName("discount")
		private BigDecimal discount = BigDecimal.ZERO; // 折扣
		@SerializedName("received")
		private BigDecimal received = BigDecimal.ZERO; // 已收金额
		@SerializedName("cod_amount")
		private BigDecimal codAmount = BigDecimal.ZERO; // COD金额
		@SerializedName("other_amount")
		private BigDecimal otherAmount = BigDecimal.ZERO; // 其他费用

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public int getDeliveryTerm()
		{
			return deliveryTerm;
		}

		public void setDeliveryTerm(int deliveryTerm)
		{
			this.deliveryTerm = deliveryTerm;
		}

		public byte getRefundStatus()
		{
			return refundStatus;
		}

		public void setRefundStatus(byte refundStatus)
		{
			this.refundStatus = refundStatus;
		}

		public byte getTradeStatus()
		{
			return tradeStatus;
		}

		public void setTradeStatus(byte tradeStatus)
		{
			this.tradeStatus = tradeStatus;
		}

		public byte getPayStatus()
		{
			return payStatus;
		}

		public void setPayStatus(byte payStatus)
		{
			this.payStatus = payStatus;
		}

		public byte getProcessStatus()
		{
			return processStatus;
		}

		public void setProcessStatus(byte processStatus)
		{
			this.processStatus = processStatus;
		}

		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public Boolean getAutoWms()
		{
			return autoWms;
		}

		public void setAutoWms(Boolean autoWms)
		{
			this.autoWms = autoWms;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Integer getOrderCount()
		{
			return orderCount;
		}

		public void setOrderCount(Integer orderCount)
		{
			this.orderCount = orderCount;
		}

		public String getTradeTime()
		{
			return tradeTime;
		}

		public void setTradeTime(String tradeTime)
		{
			this.tradeTime = tradeTime;
		}

		public Integer getRemarkFlag()
		{
			return remarkFlag;
		}

		public void setRemarkFlag(Integer remarkFlag)
		{
			this.remarkFlag = remarkFlag;
		}

		public Integer getConsignInterval()
		{
			return consignInterval;
		}

		public void setConsignInterval(Integer consignInterval)
		{
			this.consignInterval = consignInterval;
		}

		public String getToDeliverTime()
		{
			return toDeliverTime;
		}

		public void setToDeliverTime(String toDeliverTime)
		{
			this.toDeliverTime = toDeliverTime;
		}

		public BigDecimal getPlatformCost()
		{
			return platformCost;
		}

		public void setPlatformCost(BigDecimal platformCost)
		{
			this.platformCost = platformCost;
		}

		public Integer getLogisticsType()
		{
			return logisticsType;
		}

		public void setLogisticsType(Integer logisticsType)
		{
			this.logisticsType = logisticsType;
		}

		public String getEndTime()
		{
			return endTime;
		}

		public void setEndTime(String endTime)
		{
			this.endTime = endTime;
		}

		public String getPayTime()
		{
			return payTime;
		}

		public void setPayTime(String payTime)
		{
			this.payTime = payTime;
		}

		public Byte getPayMethod()
		{
			return payMethod;
		}

		public void setPayMethod(Byte payMethod)
		{
			this.payMethod = payMethod;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getPayAccount()
		{
			return payAccount;
		}

		public void setPayAccount(String payAccount)
		{
			this.payAccount = payAccount;
		}

		public String getPayId()
		{
			return payId;
		}

		public void setPayId(String payId)
		{
			this.payId = payId;
		}

		public String getReceiverZip()
		{
			return receiverZip;
		}

		public void setReceiverZip(String receiverZip)
		{
			this.receiverZip = receiverZip;
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

		public String getReceiverArea()
		{
			return receiverArea;
		}

		public void setReceiverArea(String receiverArea)
		{
			this.receiverArea = receiverArea;
		}

		public String getReceiverMobile()
		{
			return receiverMobile;
		}

		public void setReceiverMobile(String receiverMobile)
		{
			this.receiverMobile = receiverMobile;
		}

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		public String getBuyerNick()
		{
			return buyerNick;
		}

		public void setBuyerNick(String buyerNick)
		{
			this.buyerNick = buyerNick;
		}

		public String getBuyerEmail()
		{
			return buyerEmail;
		}

		public void setBuyerEmail(String buyerEmail)
		{
			this.buyerEmail = buyerEmail;
		}

		public String getBuyerMessage()
		{
			return buyerMessage;
		}

		public void setBuyerMessage(String buyerMessage)
		{
			this.buyerMessage = buyerMessage;
		}

		public String getBuyerArea()
		{
			return buyerArea;
		}

		public void setBuyerArea(String buyerArea)
		{
			this.buyerArea = buyerArea;
		}

		public Byte getInvoiceType()
		{
			return invoiceType;
		}

		public void setInvoiceType(Byte invoiceType)
		{
			this.invoiceType = invoiceType;
		}

		public String getInvoiceContent()
		{
			return invoiceContent;
		}

		public void setInvoiceContent(String invoiceContent)
		{
			this.invoiceContent = invoiceContent;
		}

		public String getInvoiceTitle()
		{
			return invoiceTitle;
		}

		public void setInvoiceTitle(String invoiceTitle)
		{
			this.invoiceTitle = invoiceTitle;
		}

		public BigDecimal getReceivable()
		{
			return receivable;
		}

		public void setReceivable(BigDecimal receivable)
		{
			this.receivable = receivable;
		}

		public BigDecimal getPostAmount()
		{
			return postAmount;
		}

		public void setPostAmount(BigDecimal postAmount)
		{
			this.postAmount = postAmount;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getReceived()
		{
			return received;
		}

		public void setReceived(BigDecimal received)
		{
			this.received = received;
		}

		public BigDecimal getCodAmount()
		{
			return codAmount;
		}

		public void setCodAmount(BigDecimal codAmount)
		{
			this.codAmount = codAmount;
		}

		public BigDecimal getOtherAmount()
		{
			return otherAmount;
		}

		public void setOtherAmount(BigDecimal otherAmount)
		{
			this.otherAmount = otherAmount;
		}
	}

	public static class RawTradeOrder
	{
		public static final byte ORDER_TYPE_NORMAL = 0; // 普通货品
		public static final byte ORDER_TYPE_VIRTUAL = 1; // 虚拟货品
		public static final byte ORDER_TYPE_SERVICE = 2; // 服务

		// 退款状态
		public static final byte REFUND_STATUS_NONE = 0; // 无退款
		public static final byte REFUND_STATUS_CANCEL = 1; // 取消退款,
		public static final byte REFUND_STATUS_APPLY = 2; // 已申请退款,
		public static final byte REFUND_STATUS_AGREE = 3; // 等待退货,
		public static final byte REFUND_STATUS_WAIT_RECEIVE = 4; // 等待收货,
		public static final byte REFUND_STATUS_SUCCESS = 5; // 退款成功
		public static final byte REFUND_STATUS_DISCARD = 6; // 未付款关闭

		// 平台状态
		public static final byte PLATFORM_STATUS = 30;

		@SerializedName("tid")
		private String tid;
		@SerializedName("oid")
		private String oid;
		@SerializedName("status")
		private Byte status;
		@SerializedName("refund_status")
		private Byte refundStatus;
		@SerializedName("goods_id")
		private String goodsId;
		@SerializedName("spec_id")
		private String specId;
		@SerializedName("goods_no")
		private String goodsNo;
		@SerializedName("spec_no")
		private String specNo;
		@SerializedName("goods_name")
		private String goodsName;
		@SerializedName("spec_name")
		private String specName;
		@SerializedName("order_type")
		private Byte orderType = ORDER_TYPE_NORMAL;
		@SerializedName("cid")
		private String cid = "";
		@SerializedName("num")
		private BigDecimal num;
		@SerializedName("price")
		private BigDecimal price;
		@SerializedName("discount")
		private BigDecimal discount;
		@SerializedName("share_discount")
		private BigDecimal shareDiscount;
		@SerializedName("adjust_amount")
		private BigDecimal adjustAmount;
		@SerializedName("refund_amount")
		private BigDecimal refundAmount;
		@SerializedName("total_amount")
		private BigDecimal totalAmount;
		@SerializedName("remark")
		private String remark;
		@SerializedName("json")
		private String json;



		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public String getOid()
		{
			return oid;
		}

		public void setOid(String oid)
		{
			this.oid = oid;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public Byte getRefundStatus()
		{
			return refundStatus;
		}

		public void setRefundStatus(Byte refundStatus)
		{
			this.refundStatus = refundStatus;
		}

		public Byte getOrderType()
		{
			return orderType;
		}

		public void setOrderType(Byte orderType)
		{
			this.orderType = orderType;
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

		public String getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(String goodsId)
		{
			this.goodsId = goodsId;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public String getSpecId()
		{
			return specId;
		}

		public void setSpecId(String specId)
		{
			this.specId = specId;
		}

		public String getJson()
		{
			return json;
		}

		public void setJson(String json)
		{
			this.json = json;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getCid()
		{
			return cid;
		}

		public void setCid(String cid)
		{
			this.cid = cid;
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

		public BigDecimal getShareDiscount()
		{
			return shareDiscount;
		}

		public void setShareDiscount(BigDecimal shareDiscount)
		{
			this.shareDiscount = shareDiscount;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getAdjustAmount()
		{
			return adjustAmount;
		}

		public void setAdjustAmount(BigDecimal adjustAmount)
		{
			this.adjustAmount = adjustAmount;
		}

		public BigDecimal getRefundAmount()
		{
			return refundAmount;
		}

		public void setRefundAmount(BigDecimal refundAmount)
		{
			this.refundAmount = refundAmount;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
		}
	}

	public static class RawTradeDiscount
	{
		@SerializedName("tid")
		private String tid;
		@SerializedName("oid")
		private String oid;
		@SerializedName("sn")
		private String sn;
		@SerializedName("type")
		private String type;
		@SerializedName("name")
		private String name;
		@SerializedName("is_bonus")
		private Byte isBonus;
		@SerializedName("detail")
		private String detail;
		@SerializedName("amount")
		private BigDecimal amount;

		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public String getOid()
		{
			return oid;
		}

		public void setOid(String oid)
		{
			this.oid = oid;
		}

		public String getSn()
		{
			return sn;
		}

		public void setSn(String sn)
		{
			this.sn = sn;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public Byte getIsBonus()
		{
			return isBonus;
		}

		public void setIsBonus(Byte isBonus)
		{
			this.isBonus = isBonus;
		}

		public String getDetail()
		{
			return detail;
		}

		public void setDetail(String detail)
		{
			this.detail = detail;
		}

		public BigDecimal getAmount()
		{
			return amount;
		}

		public void setAmount(BigDecimal amount)
		{
			this.amount = amount;
		}
	}

	public String getShopNo()
	{
		return shopNo;
	}

	public void setShopNo(String shopNo)
	{
		this.shopNo = shopNo;
	}
}
