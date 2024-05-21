package cn.wangdian.erp.sdk.api.sales.dto;

import java.math.BigDecimal;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class StockOutQueryResponse
{
	@SerializedName("total_count")
	private int totalCount;

	@SerializedName("order")
	private List<OrderItem> orders;

	public static class OrderItem
	{

		// 1无库存记录 2地址发生变化 4发票变化 8仓库变化16备注变化32平台更换货品64退款...512抢单异常
		public static final int BAD_REASON_ADDRESS_CHANGE = 2;
		public static final int BAD_REASON_INVOICE_CHANGE = 4;
		public static final int BAD_REASON_WAREHOUSE_CHANGE = 8;
		public static final int BAD_REASON_REMARK_CHANGE = 16; // 客服备注
		public static final int BAD_REASON_GOODS_CHANGE = 32;
		// public static final int BAD_REASON_REFUND = 64;
		// //申请退款,订单审核时通过refund_status判断
		public static final int BAD_REASON_BLOCK_GIFT = 128;
		public static final int BAD_REASON_BLOCK_SWAP = 256; // 拦截换货
		// (1.换货订单因再次申请退款而拦截
		// 2.平台换货单取消/换改退)
		public static final int BAD_REASON_BUYER_MESSAGE_CHANGE = 512; // 客户备注发生变化
		public static final int BAD_REASON_BLOCK_SYNC_LOGISTICS = 1024; // 拦截平台已发货。

		@SerializedName("logistics_name")
		private String logisticsName;

		@SerializedName("details_list")
		private List<DetailsItem> details;

		@SerializedName("consign_time")
		private String consignTime;

		@SerializedName("post_amount")
		private BigDecimal postAmount;

		@SerializedName("trade_time")
		private Long tradeTime;

		@SerializedName("receiver_dtb")
		private String receiverDtb;

		@SerializedName("bad_reason")
		private Integer badReason;

		@SerializedName("prInteger_remark")
		private String prIntegerRemark;

		@SerializedName("employee_no")
		private String employeeNo;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		@SerializedName("trade_id")
		private Integer tradeId;

		@SerializedName("trade_label")
		private String tradeLabel;

		@SerializedName("shop_remark")
		private String shopRemark;

		@SerializedName("invoice_id")
		private Integer invoiceId;

		@SerializedName("modified")
		private String modified;

		@SerializedName("receiver_country")
		private Integer receiverCountry;

		@SerializedName("order_type")
		private Integer orderType;

		@SerializedName("shop_no")
		private String shopNo;

		@SerializedName("seq_no")
		private Integer seqNo;

		@SerializedName("receiver_area")
		private String receiverArea;

		@SerializedName("customer_no")
		private String customerNo;

		@SerializedName("refund_status")
		private Integer refundStatus;

		@SerializedName("receiver_province")
		private Integer receiverProvince;

		@SerializedName("buyer_message")
		private String buyerMessage;

		@SerializedName("created")
		private Long created;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("block_reason")
		private Integer blockReason;

		@SerializedName("tax")
		private BigDecimal tax;

		@SerializedName("logistics_code")
		private String logisticsCode;

		@SerializedName("shop_name")
		private String shopName;

		@SerializedName("shop_platform_id")
		private Integer shopPlatformId;

		@SerializedName("pay_time")
		private Long payTime;

		@SerializedName("shop_id")
		private Integer shopId;

		@SerializedName("warehouse_name")
		private String warehouseName;

		@SerializedName("goods_total_cost")
		private BigDecimal goodsTotalCost;

		@SerializedName("nick_name")
		private String nickName;

		@SerializedName("trade_no")
		private String tradeNo;

		@SerializedName("id_card_type")
		private Integer idCardType;

		@SerializedName("status")
		private Integer status;

		@SerializedName("package_fee")
		private BigDecimal packageFee;

		@SerializedName("order_no")
		private String orderNo;

		@SerializedName("src_trade_no")
		private String srcTradeNo;

		@SerializedName("post_fee")
		private BigDecimal postFee;

		@SerializedName("receiver_city")
		private Integer receiverCity;

		@SerializedName("invoice_title")
		private String invoiceTitle;

		@SerializedName("id_card")
		private String idCard;

		@SerializedName("remark")
		private String remark;

		@SerializedName("sub_platform_id")
		private Integer subPlatformId;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("cod_amount")
		private BigDecimal codAmount;

		@SerializedName("flag_name")
		private String flagName;

		@SerializedName("src_order_no")
		private String srcOrderNo;

		@SerializedName("warehouse_no")
		private String warehouseNo;

		@SerializedName("receiver_telno")
		private String receiverTelno;

		@SerializedName("receiver_zip")
		private String receiverZip;

		@SerializedName("invoice_content")
		private String invoiceContent;

		@SerializedName("trade_status")
		private Integer tradeStatus;

		@SerializedName("receiver_name")
		private String receiverName;

		@SerializedName("invoice_type")
		private Integer invoiceType;

		@SerializedName("currency")
		private String currency;

		@SerializedName("logistics_type")
		private Integer logisticsType;

		@SerializedName("trade_from")
		private Integer tradeFrom;

		@SerializedName("delivery_term")
		private Integer deliveryTerm;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("receiver_district")
		private Integer receiverDistrict;

		@SerializedName("goods_total_amount")
		private BigDecimal goodsTotalAmount;

		@SerializedName("receivable")
		private BigDecimal receivable;

		@SerializedName("receiver_mobile")
		private String receiverMobile;

		@SerializedName("salesman_no")
		private String salesmanNo;

		@SerializedName("stock_check_time")
		private Long stockCheckTime;

		@SerializedName("cs_remark")
		private String csRemark;

		@SerializedName("print_remark")
		private String printRemark;

		@SerializedName("platform_id")
		private Integer platformId;

		@SerializedName("receiver_address")
		private String receiverAddress;

		@SerializedName("trade_type")
		private Integer tradeType;

		@SerializedName("fullname")
		private String fullname;

		@SerializedName("customer_name")
		private String customerName;

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public List<DetailsItem> getDetails()
		{
			return details;
		}

		public void setDetails(List<DetailsItem> details)
		{
			this.details = details;
		}

		public String getConsignTime()
		{
			return consignTime;
		}

		public void setConsignTime(String consignTime)
		{
			this.consignTime = consignTime;
		}

		public BigDecimal getPostAmount()
		{
			return postAmount;
		}

		public void setPostAmount(BigDecimal postAmount)
		{
			this.postAmount = postAmount;
		}

		public Long getTradeTime()
		{
			return tradeTime;
		}

		public void setTradeTime(Long tradeTime)
		{
			this.tradeTime = tradeTime;
		}

		public String getReceiverDtb()
		{
			return receiverDtb;
		}

		public void setReceiverDtb(String receiverDtb)
		{
			this.receiverDtb = receiverDtb;
		}

		public Integer getBadReason()
		{
			return badReason;
		}

		public void setBadReason(Integer badReason)
		{
			this.badReason = badReason;
		}

		public String getPrIntegerRemark()
		{
			return prIntegerRemark;
		}

		public void setPrIntegerRemark(String prIntegerRemark)
		{
			this.prIntegerRemark = prIntegerRemark;
		}

		public String getEmployeeNo()
		{
			return employeeNo;
		}

		public void setEmployeeNo(String employeeNo)
		{
			this.employeeNo = employeeNo;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}

		public Integer getTradeId()
		{
			return tradeId;
		}

		public void setTradeId(Integer tradeId)
		{
			this.tradeId = tradeId;
		}

		public String getTradeLabel()
		{
			return tradeLabel;
		}

		public void setTradeLabel(String tradeLabel)
		{
			this.tradeLabel = tradeLabel;
		}

		public String getShopRemark()
		{
			return shopRemark;
		}

		public void setShopRemark(String shopRemark)
		{
			this.shopRemark = shopRemark;
		}

		public Integer getInvoiceId()
		{
			return invoiceId;
		}

		public void setInvoiceId(Integer invoiceId)
		{
			this.invoiceId = invoiceId;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
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

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public Integer getSeqNo()
		{
			return seqNo;
		}

		public void setSeqNo(Integer seqNo)
		{
			this.seqNo = seqNo;
		}

		public String getReceiverArea()
		{
			return receiverArea;
		}

		public void setReceiverArea(String receiverArea)
		{
			this.receiverArea = receiverArea;
		}

		public String getCustomerNo()
		{
			return customerNo;
		}

		public void setCustomerNo(String customerNo)
		{
			this.customerNo = customerNo;
		}

		public Integer getRefundStatus()
		{
			return refundStatus;
		}

		public void setRefundStatus(Integer refundStatus)
		{
			this.refundStatus = refundStatus;
		}

		public Integer getReceiverProvince()
		{
			return receiverProvince;
		}

		public void setReceiverProvince(Integer receiverProvince)
		{
			this.receiverProvince = receiverProvince;
		}

		public String getBuyerMessage()
		{
			return buyerMessage;
		}

		public void setBuyerMessage(String buyerMessage)
		{
			this.buyerMessage = buyerMessage;
		}

		public Long getCreated()
		{
			return created;
		}

		public void setCreated(Long created)
		{
			this.created = created;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public Integer getBlockReason()
		{
			return blockReason;
		}

		public void setBlockReason(Integer blockReason)
		{
			this.blockReason = blockReason;
		}

		public BigDecimal getTax()
		{
			return tax;
		}

		public void setTax(BigDecimal tax)
		{
			this.tax = tax;
		}

		public String getLogisticsCode()
		{
			return logisticsCode;
		}

		public void setLogisticsCode(String logisticsCode)
		{
			this.logisticsCode = logisticsCode;
		}

		public String getShopName()
		{
			return shopName;
		}

		public void setShopName(String shopName)
		{
			this.shopName = shopName;
		}

		public Integer getShopPlatformId()
		{
			return shopPlatformId;
		}

		public void setShopPlatformId(Integer shopPlatformId)
		{
			this.shopPlatformId = shopPlatformId;
		}

		public Long getPayTime()
		{
			return payTime;
		}

		public void setPayTime(Long payTime)
		{
			this.payTime = payTime;
		}

		public Integer getShopId()
		{
			return shopId;
		}

		public void setShopId(Integer shopId)
		{
			this.shopId = shopId;
		}

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
		}

		public BigDecimal getGoodsTotalCost()
		{
			return goodsTotalCost;
		}

		public void setGoodsTotalCost(BigDecimal goodsTotalCost)
		{
			this.goodsTotalCost = goodsTotalCost;
		}

		public String getNickName()
		{
			return nickName;
		}

		public void setNickName(String nickName)
		{
			this.nickName = nickName;
		}

		public String getTradeNo()
		{
			return tradeNo;
		}

		public void setTradeNo(String tradeNo)
		{
			this.tradeNo = tradeNo;
		}

		public Integer getIdCardType()
		{
			return idCardType;
		}

		public void setIdCardType(Integer idCardType)
		{
			this.idCardType = idCardType;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
		}

		public BigDecimal getPackageFee()
		{
			return packageFee;
		}

		public void setPackageFee(BigDecimal packageFee)
		{
			this.packageFee = packageFee;
		}

		public String getOrderNo()
		{
			return orderNo;
		}

		public void setOrderNo(String orderNo)
		{
			this.orderNo = orderNo;
		}

		public String getSrcTradeNo()
		{
			return srcTradeNo;
		}

		public void setSrcTradeNo(String srcTradeNo)
		{
			this.srcTradeNo = srcTradeNo;
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

		public String getInvoiceTitle()
		{
			return invoiceTitle;
		}

		public void setInvoiceTitle(String invoiceTitle)
		{
			this.invoiceTitle = invoiceTitle;
		}

		public String getIdCard()
		{
			return idCard;
		}

		public void setIdCard(String idCard)
		{
			this.idCard = idCard;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Integer getSubPlatformId()
		{
			return subPlatformId;
		}

		public void setSubPlatformId(Integer subPlatformId)
		{
			this.subPlatformId = subPlatformId;
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

		public BigDecimal getCodAmount()
		{
			return codAmount;
		}

		public void setCodAmount(BigDecimal codAmount)
		{
			this.codAmount = codAmount;
		}

		public String getFlagName()
		{
			return flagName;
		}

		public void setFlagName(String flagName)
		{
			this.flagName = flagName;
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

		public String getInvoiceContent()
		{
			return invoiceContent;
		}

		public void setInvoiceContent(String invoiceContent)
		{
			this.invoiceContent = invoiceContent;
		}

		public Integer getTradeStatus()
		{
			return tradeStatus;
		}

		public void setTradeStatus(Integer tradeStatus)
		{
			this.tradeStatus = tradeStatus;
		}

		public String getReceiverName()
		{
			return receiverName;
		}

		public void setReceiverName(String receiverName)
		{
			this.receiverName = receiverName;
		}

		public Integer getInvoiceType()
		{
			return invoiceType;
		}

		public void setInvoiceType(Integer invoiceType)
		{
			this.invoiceType = invoiceType;
		}

		public String getCurrency()
		{
			return currency;
		}

		public void setCurrency(String currency)
		{
			this.currency = currency;
		}

		public Integer getLogisticsType()
		{
			return logisticsType;
		}

		public void setLogisticsType(Integer logisticsType)
		{
			this.logisticsType = logisticsType;
		}

		public Integer getTradeFrom()
		{
			return tradeFrom;
		}

		public void setTradeFrom(Integer tradeFrom)
		{
			this.tradeFrom = tradeFrom;
		}

		public Integer getDeliveryTerm()
		{
			return deliveryTerm;
		}

		public void setDeliveryTerm(Integer deliveryTerm)
		{
			this.deliveryTerm = deliveryTerm;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public Integer getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(Integer receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
		}

		public BigDecimal getGoodsTotalAmount()
		{
			return goodsTotalAmount;
		}

		public void setGoodsTotalAmount(BigDecimal goodsTotalAmount)
		{
			this.goodsTotalAmount = goodsTotalAmount;
		}

		public BigDecimal getReceivable()
		{
			return receivable;
		}

		public void setReceivable(BigDecimal receivable)
		{
			this.receivable = receivable;
		}

		public String getReceiverMobile()
		{
			return receiverMobile;
		}

		public void setReceiverMobile(String receiverMobile)
		{
			this.receiverMobile = receiverMobile;
		}

		public String getSalesmanNo()
		{
			return salesmanNo;
		}

		public void setSalesmanNo(String salesmanNo)
		{
			this.salesmanNo = salesmanNo;
		}

		public Long getStockCheckTime()
		{
			return stockCheckTime;
		}

		public void setStockCheckTime(Long stockCheckTime)
		{
			this.stockCheckTime = stockCheckTime;
		}

		public String getCsRemark()
		{
			return csRemark;
		}

		public void setCsRemark(String csRemark)
		{
			this.csRemark = csRemark;
		}

		public String getPrintRemark()
		{
			return printRemark;
		}

		public void setPrintRemark(String printRemark)
		{
			this.printRemark = printRemark;
		}

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		public Integer getTradeType()
		{
			return tradeType;
		}

		public void setTradeType(Integer tradeType)
		{
			this.tradeType = tradeType;
		}

		public String getFullname()
		{
			return fullname;
		}

		public void setFullname(String fullname)
		{
			this.fullname = fullname;
		}

		public String getCustomerName()
		{
			return customerName;
		}

		public void setCustomerName(String customerName)
		{
			this.customerName = customerName;
		}
	}

	public static class DetailsItem
	{
		@SerializedName("spec_code")
		private String specCode;

		@SerializedName("sell_price")
		private BigDecimal sellPrice;

		@SerializedName("goods_no")
		private String goodsNo;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("remark")
		private String remark;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("stockout_id")
		private Integer stockoutId;

		@SerializedName("position_id")
		private Integer positionId;

		@SerializedName("position_no")
		private Integer positionNo;

		@SerializedName("position_goods_count")
		private BigDecimal positionGoodsCount;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		@SerializedName("spec_id")
		private Integer specId;

		@SerializedName("barcode")
		private String barcode;

		@SerializedName("unit_name")
		private String unitName;

		@SerializedName("cost_price")
		private BigDecimal costPrice;

		@SerializedName("goods_name")
		private String goodsName;

		@SerializedName("src_tid")
		private String srcTid;

		@SerializedName("refund_status")
		private Integer refundStatus;

		@SerializedName("batch_no")
		private String batchNo;

		@SerializedName("goods_amount")
		private Integer goodsAmount;

		@SerializedName("good_prop3")
		private String goodProp3;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("goods_id")
		private Integer goodsId;

		@SerializedName("good_prop4")
		private String goodProp4;

		@SerializedName("gift_type")
		private Integer giftType;

		@SerializedName("from_mask")
		private Integer fromMask;

		@SerializedName("good_prop1")
		private String goodProp1;

		@SerializedName("good_prop2")
		private String goodProp2;

		@SerializedName("rec_id")
		private Integer recId;

		@SerializedName("prop6")
		private String prop6;

		@SerializedName("prop5")
		private String prop5;

		@SerializedName("prop4")
		private String prop4;

		@SerializedName("prop3")
		private String prop3;

		@SerializedName("prop2")
		private String prop2;

		@SerializedName("prop1")
		private String prop1;

		@SerializedName("sale_order_id")
		private Integer saleOrderId;

		@SerializedName("expire_date")
		private String expireDate;

		@SerializedName("total_amount")
		private BigDecimal totalAmount;

		@SerializedName("good_prop5")
		private String goodProp5;

		@SerializedName("good_prop6")
		private String goodProp6;

		@SerializedName("paid")
		private BigDecimal paid;

		@SerializedName("platform_id")
		private Integer platformId;

		@SerializedName("market_price")
		private BigDecimal marketPrice;

		@SerializedName("src_oid")
		private String srcOid;

		@SerializedName("spec_name")
		private String specName;

		@SerializedName("sn_list")
		private String snList;

		@SerializedName("goods_type")
		private Integer goodsType;

		@SerializedName("share_amount")
		private BigDecimal shareAmount;

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public BigDecimal getSellPrice()
		{
			return sellPrice;
		}

		public void setSellPrice(BigDecimal sellPrice)
		{
			this.sellPrice = sellPrice;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
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

		public Integer getPositionId()
		{
			return positionId;
		}

		public void setPositionId(Integer positionId)
		{
			this.positionId = positionId;
		}

		public Integer getPositionNo()
		{
			return positionNo;
		}

		public void setPositionNo(Integer positionNo)
		{
			this.positionNo = positionNo;
		}

		public BigDecimal getPositionGoodsCount()
		{
			return positionGoodsCount;
		}

		public void setPositionGoodsCount(BigDecimal positionGoodsCount)
		{
			this.positionGoodsCount = positionGoodsCount;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}

		public Integer getSpecId()
		{
			return specId;
		}

		public void setSpecId(Integer specId)
		{
			this.specId = specId;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getUnitName()
		{
			return unitName;
		}

		public void setUnitName(String unitName)
		{
			this.unitName = unitName;
		}

		public BigDecimal getCostPrice()
		{
			return costPrice;
		}

		public void setCostPrice(BigDecimal costPrice)
		{
			this.costPrice = costPrice;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getSrcTid()
		{
			return srcTid;
		}

		public void setSrcTid(String srcTid)
		{
			this.srcTid = srcTid;
		}

		public Integer getRefundStatus()
		{
			return refundStatus;
		}

		public void setRefundStatus(Integer refundStatus)
		{
			this.refundStatus = refundStatus;
		}

		public String getBatchNo()
		{
			return batchNo;
		}

		public void setBatchNo(String batchNo)
		{
			this.batchNo = batchNo;
		}

		public Integer getGoodsAmount()
		{
			return goodsAmount;
		}

		public void setGoodsAmount(Integer goodsAmount)
		{
			this.goodsAmount = goodsAmount;
		}

		public String getGoodProp3()
		{
			return goodProp3;
		}

		public void setGoodProp3(String goodProp3)
		{
			this.goodProp3 = goodProp3;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public Integer getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(Integer goodsId)
		{
			this.goodsId = goodsId;
		}

		public String getGoodProp4()
		{
			return goodProp4;
		}

		public void setGoodProp4(String goodProp4)
		{
			this.goodProp4 = goodProp4;
		}

		public Integer getGiftType()
		{
			return giftType;
		}

		public void setGiftType(Integer giftType)
		{
			this.giftType = giftType;
		}

		public Integer getFromMask()
		{
			return fromMask;
		}

		public void setFromMask(Integer fromMask)
		{
			this.fromMask = fromMask;
		}

		public String getGoodProp1()
		{
			return goodProp1;
		}

		public void setGoodProp1(String goodProp1)
		{
			this.goodProp1 = goodProp1;
		}

		public String getGoodProp2()
		{
			return goodProp2;
		}

		public void setGoodProp2(String goodProp2)
		{
			this.goodProp2 = goodProp2;
		}

		public Integer getRecId()
		{
			return recId;
		}

		public void setRecId(Integer recId)
		{
			this.recId = recId;
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

		public Integer getSaleOrderId()
		{
			return saleOrderId;
		}

		public void setSaleOrderId(Integer saleOrderId)
		{
			this.saleOrderId = saleOrderId;
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

		public String getGoodProp5()
		{
			return goodProp5;
		}

		public void setGoodProp5(String goodProp5)
		{
			this.goodProp5 = goodProp5;
		}

		public String getGoodProp6()
		{
			return goodProp6;
		}

		public void setGoodProp6(String goodProp6)
		{
			this.goodProp6 = goodProp6;
		}

		public BigDecimal getPaid()
		{
			return paid;
		}

		public void setPaid(BigDecimal paid)
		{
			this.paid = paid;
		}

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public BigDecimal getMarketPrice()
		{
			return marketPrice;
		}

		public void setMarketPrice(BigDecimal marketPrice)
		{
			this.marketPrice = marketPrice;
		}

		public String getSrcOid()
		{
			return srcOid;
		}

		public void setSrcOid(String srcOid)
		{
			this.srcOid = srcOid;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getSnList()
		{
			return snList;
		}

		public void setSnList(String snList)
		{
			this.snList = snList;
		}

		public Integer getGoodsType()
		{
			return goodsType;
		}

		public void setGoodsType(Integer goodsType)
		{
			this.goodsType = goodsType;
		}

		public BigDecimal getShareAmount()
		{
			return shareAmount;
		}

		public void setShareAmount(BigDecimal shareAmount)
		{
			this.shareAmount = shareAmount;
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