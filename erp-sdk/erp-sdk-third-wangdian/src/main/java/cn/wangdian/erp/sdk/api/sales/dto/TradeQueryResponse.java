package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@ToString
public class TradeQueryResponse
{

	@SerializedName("total_count")
	private Integer totalCount;

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

		@SerializedName("warehouse_type")
		private Integer warehouseType;

		@SerializedName("fenxiao_nick")
		private String fenxiaoNick;

		@SerializedName("post_amount")
		private BigDecimal postAmount;

		@SerializedName("trade_time")
		private String tradeTime;

		@SerializedName("receiver_ring")
		private String receiverRing;

		@SerializedName("raw_goods_type_count")
		private Integer rawGoodsTypeCount;

		@SerializedName("remark_flag")
		private Integer remarkFlag;

		@SerializedName("receiver_dtb")
		private String receiverDtb;

		@SerializedName("detail_list")
		private List<DetailItem> details;

		@SerializedName("bad_reason")
		private Integer badReason;

		@SerializedName("print_remark")
		private String printRemark;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("other_amount")
		private BigDecimal otherAmount;

		@SerializedName("pay_account")
		private String payAccount;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		@SerializedName("raw_goods_count")
		private BigDecimal rawGoodsCount;

		@SerializedName("to_deliver_time")
		private String toDeliverTime;

		@SerializedName("consign_time")
		private String consignTime;

		@SerializedName("fchecker_name")
		private String fcheckerName;

		@SerializedName("trade_label")
		private String tradeLabel;

		@SerializedName("shop_remark")
		private String shopRemark;

		@SerializedName("invoice_id")
		private Integer invoiceId;

		@SerializedName("modified")
		private String modified;

		@SerializedName("shop_no")
		private String shopNo;

		@SerializedName("checker_name")
		private String checkerName;

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
		private String created;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("tax")
		private BigDecimal tax;

		@SerializedName("logistics_code")
		private String logisticsCode;

		@SerializedName("shop_name")
		private String shopName;

		@SerializedName("shop_platform_id")
		private Integer shopPlatformId;

		@SerializedName("pay_time")
		private String payTime;

		@SerializedName("src_tids")
		private String srcTids;

		@SerializedName("shop_id")
		private Integer shopId;

		@SerializedName("checkouter_name")
		private String checkouterName;

		@SerializedName("trade_no")
		private String tradeNo;

		@SerializedName("id_card_type")
		private Integer idCardType;

		@SerializedName("id_card")
		private Integer idCard;

		@SerializedName("freeze_reason")
		private String freezeReason;

		@SerializedName("single_spec_no")
		private String singleSpecNo;

		@SerializedName("salesman_name")
		private String salesmanName;

		@SerializedName("receiver_city")
		private Integer receiverCity;

		@SerializedName("invoice_title")
		private String invoiceTitle;

		@SerializedName("goods_type_count")
		private BigDecimal goodsTypeCount;

		@SerializedName("sub_platform_id")
		private Integer subPlatformId;

		@SerializedName("goods_count")
		private BigDecimal goodsCount;

		@SerializedName("cod_amount")
		private BigDecimal codAmount;

		@SerializedName("flag_name")
		private String flagName;

		@SerializedName("receiver_telno")
		private String receiverTelno;

		@SerializedName("receiver_zip")
		private String receiverZip;

		@SerializedName("warehouse_no")
		private String warehouseNo;

		@SerializedName("invoice_content")
		private String invoiceContent;

		@SerializedName("trade_status")
		private Integer tradeStatus;

		@SerializedName("post_cost")
		private BigDecimal postCost;

		@SerializedName("receiver_name")
		private String receiverName;

		@SerializedName("commission")
		private BigDecimal commission;

		@SerializedName("invoice_type")
		private Integer invoiceType;

		@SerializedName("currency")
		private String currency;

		@SerializedName("profit")
		private BigDecimal profit;

		@SerializedName("trade_from")
		private Integer tradeFrom;

		@SerializedName("delivery_term")
		private Integer deliveryTerm;

		@SerializedName("logistics_no")
		private String logisticsNo;

		@SerializedName("goods_amount")
		private BigDecimal goodsAmount;

		@SerializedName("goods_cost")
		private BigDecimal goodsCost;

		@SerializedName("receiver_district")
		private Integer receiverDistrict;

		@SerializedName("stockout_no")
		private String stockoutNo;

		@SerializedName("receivable")
		private BigDecimal receivable;

		@SerializedName("version_id")
		private Integer versionId;

		@SerializedName("receiver_mobile")
		private String receiverMobile;

		@SerializedName("buyer_nick")
		private String buyerNick;

		@SerializedName("fenxiao_type")
		private Integer fenxiaoType;

		@SerializedName("cs_remark")
		private String csRemark;

		@SerializedName("platform_id")
		private Integer platformId;

		@SerializedName("trade_type")
		private Integer tradeType;

		@SerializedName("receiver_address")
		private String receiverAddress;

		@SerializedName("logistics_type_name")
		private String logisticsTypeName;

		@SerializedName("ext_cod_fee")
		private BigDecimal extCodFee;

		@SerializedName("customer_name")
		private String customerName;

		@SerializedName("delay_to_time")
		private String delayToTime;

		@SerializedName("logistics_id")
		private String logisticsId;

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public Integer getWarehouseType()
		{
			return warehouseType;
		}

		public void setWarehouseType(Integer warehouseType)
		{
			this.warehouseType = warehouseType;
		}

		public String getFenxiaoNick()
		{
			return fenxiaoNick;
		}

		public void setFenxiaoNick(String fenxiaoNick)
		{
			this.fenxiaoNick = fenxiaoNick;
		}

		public BigDecimal getPostAmount()
		{
			return postAmount;
		}

		public void setPostAmount(BigDecimal postAmount)
		{
			this.postAmount = postAmount;
		}

		public String getTradeTime()
		{
			return tradeTime;
		}

		public void setTradeTime(String tradeTime)
		{
			this.tradeTime = tradeTime;
		}

		public String getReceiverRing()
		{
			return receiverRing;
		}

		public void setReceiverRing(String receiverRing)
		{
			this.receiverRing = receiverRing;
		}

		public Integer getRawGoodsTypeCount()
		{
			return rawGoodsTypeCount;
		}

		public void setRawGoodsTypeCount(Integer rawGoodsTypeCount)
		{
			this.rawGoodsTypeCount = rawGoodsTypeCount;
		}

		public Integer getRemarkFlag()
		{
			return remarkFlag;
		}

		public void setRemarkFlag(Integer remarkFlag)
		{
			this.remarkFlag = remarkFlag;
		}

		public String getReceiverDtb()
		{
			return receiverDtb;
		}

		public void setReceiverDtb(String receiverDtb)
		{
			this.receiverDtb = receiverDtb;
		}

		public List<DetailItem> getDetailList()
		{
			return details;
		}

		public void setDetailList(List<DetailItem> details)
		{
			this.details = details;
		}

		public Integer getBadReason()
		{
			return badReason;
		}

		public void setBadReason(Integer badReason)
		{
			this.badReason = badReason;
		}

		public String getPrintRemark()
		{
			return printRemark;
		}

		public void setPrintRemark(String printRemark)
		{
			this.printRemark = printRemark;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getOtherAmount()
		{
			return otherAmount;
		}

		public void setOtherAmount(BigDecimal otherAmount)
		{
			this.otherAmount = otherAmount;
		}

		public String getPayAccount()
		{
			return payAccount;
		}

		public void setPayAccount(String payAccount)
		{
			this.payAccount = payAccount;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}

		public BigDecimal getRawGoodsCount()
		{
			return rawGoodsCount;
		}

		public void setRawGoodsCount(BigDecimal rawGoodsCount)
		{
			this.rawGoodsCount = rawGoodsCount;
		}

		public String getToDeliverTime()
		{
			return toDeliverTime;
		}

		public void setToDeliverTime(String toDeliverTime)
		{
			this.toDeliverTime = toDeliverTime;
		}

		public String getConsignTime()
		{
			return consignTime;
		}

		public void setConsignTime(String consignTime)
		{
			this.consignTime = consignTime;
		}

		public String getFcheckerName()
		{
			return fcheckerName;
		}

		public void setFcheckerName(String fcheckerName)
		{
			this.fcheckerName = fcheckerName;
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

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public String getCheckerName()
		{
			return checkerName;
		}

		public void setCheckerName(String checkerName)
		{
			this.checkerName = checkerName;
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

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
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

		public String getPayTime()
		{
			return payTime;
		}

		public void setPayTime(String payTime)
		{
			this.payTime = payTime;
		}

		public String getSrcTids()
		{
			return srcTids;
		}

		public void setSrcTids(String srcTids)
		{
			this.srcTids = srcTids;
		}

		public Integer getShopId()
		{
			return shopId;
		}

		public void setShopId(Integer shopId)
		{
			this.shopId = shopId;
		}

		public String getCheckouterName()
		{
			return checkouterName;
		}

		public void setCheckouterName(String checkouterName)
		{
			this.checkouterName = checkouterName;
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

		public Integer getIdCard()
		{
			return idCard;
		}

		public void setIdCard(Integer idCard)
		{
			this.idCard = idCard;
		}

		public String getFreezeReason()
		{
			return freezeReason;
		}

		public void setFreezeReason(String freezeReason)
		{
			this.freezeReason = freezeReason;
		}

		public String getSingleSpecNo()
		{
			return singleSpecNo;
		}

		public void setSingleSpecNo(String singleSpecNo)
		{
			this.singleSpecNo = singleSpecNo;
		}

		public String getSalesmanName()
		{
			return salesmanName;
		}

		public void setSalesmanName(String salesmanName)
		{
			this.salesmanName = salesmanName;
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

		public BigDecimal getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(BigDecimal goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
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

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
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

		public BigDecimal getPostCost()
		{
			return postCost;
		}

		public void setPostCost(BigDecimal postCost)
		{
			this.postCost = postCost;
		}

		public String getReceiverName()
		{
			return receiverName;
		}

		public void setReceiverName(String receiverName)
		{
			this.receiverName = receiverName;
		}

		public BigDecimal getCommission()
		{
			return commission;
		}

		public void setCommission(BigDecimal commission)
		{
			this.commission = commission;
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

		public BigDecimal getProfit()
		{
			return profit;
		}

		public void setProfit(BigDecimal profit)
		{
			this.profit = profit;
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

		public BigDecimal getGoodsAmount()
		{
			return goodsAmount;
		}

		public void setGoodsAmount(BigDecimal goodsAmount)
		{
			this.goodsAmount = goodsAmount;
		}

		public BigDecimal getGoodsCost()
		{
			return goodsCost;
		}

		public void setGoodsCost(BigDecimal goodsCost)
		{
			this.goodsCost = goodsCost;
		}

		public Integer getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(Integer receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
		}

		public String getStockoutNo()
		{
			return stockoutNo;
		}

		public void setStockoutNo(String stockoutNo)
		{
			this.stockoutNo = stockoutNo;
		}

		public BigDecimal getReceivable()
		{
			return receivable;
		}

		public void setReceivable(BigDecimal receivable)
		{
			this.receivable = receivable;
		}

		public Integer getVersionId()
		{
			return versionId;
		}

		public void setVersionId(Integer versionId)
		{
			this.versionId = versionId;
		}

		public String getReceiverMobile()
		{
			return receiverMobile;
		}

		public void setReceiverMobile(String receiverMobile)
		{
			this.receiverMobile = receiverMobile;
		}

		public String getBuyerNick()
		{
			return buyerNick;
		}

		public void setBuyerNick(String buyerNick)
		{
			this.buyerNick = buyerNick;
		}

		public Integer getFenxiaoType()
		{
			return fenxiaoType;
		}

		public void setFenxiaoType(Integer fenxiaoType)
		{
			this.fenxiaoType = fenxiaoType;
		}

		public String getCsRemark()
		{
			return csRemark;
		}

		public void setCsRemark(String csRemark)
		{
			this.csRemark = csRemark;
		}

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public Integer getTradeType()
		{
			return tradeType;
		}

		public void setTradeType(Integer tradeType)
		{
			this.tradeType = tradeType;
		}

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		public String getLogisticsTypeName()
		{
			return logisticsTypeName;
		}

		public void setLogisticsTypeName(String logisticsTypeName)
		{
			this.logisticsTypeName = logisticsTypeName;
		}

		public BigDecimal getExtCodFee()
		{
			return extCodFee;
		}

		public void setExtCodFee(BigDecimal extCodFee)
		{
			this.extCodFee = extCodFee;
		}

		public String getCustomerName()
		{
			return customerName;
		}

		public void setCustomerName(String customerName)
		{
			this.customerName = customerName;
		}

		public String getDelayToTime()
		{
			return delayToTime;
		}

		public void setDelayToTime(String delayToTime)
		{
			this.delayToTime = delayToTime;
		}

		public String getLogisticsId()
		{
			return logisticsId;
		}

		public void setLogisticsId(String logisticsId)
		{
			this.logisticsId = logisticsId;
		}
	}

	public static class DetailItem
	{
		@SerializedName("suite_discount")
		private BigDecimal suiteDiscount;

		@SerializedName("spec_code")
		private String specCode;

		@SerializedName("num")
		private BigDecimal num;

		@SerializedName("goods_no")
		private String goodsNo;

		@SerializedName("suite_name")
		private String suiteName;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("api_spec_id")
		private String apiSpecId;

		@SerializedName("suite_amount")
		private BigDecimal suiteAmount;

		@SerializedName("api_spec_name")
		private String apiSpecName;

		@SerializedName("remark")
		private String remark;

		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		@SerializedName("api_goods_name")
		private String apiGoodsName;

		@SerializedName("price")
		private BigDecimal price;

		@SerializedName("modified")
		private String modified;

		@SerializedName("api_goods_id")
		private String apiGoodsId;

		@SerializedName("commission")
		private BigDecimal commission;

		@SerializedName("guarantee_mode")
		private Integer guaranteeMode;

		@SerializedName("src_tid")
		private String srcTid;

		@SerializedName("goods_name")
		private String goodsName;

		@SerializedName("refund_status")
		private Integer refundStatus;

		@SerializedName("delivery_term")
		private Integer deliveryTerm;

		@SerializedName("created")
		private String created;

		@SerializedName("weight")
		private BigDecimal weight;

		@SerializedName("gift_type")
		private Integer giftType;

		@SerializedName("order_price")
		private BigDecimal orderPrice;

		@SerializedName("from_mask")
		private Integer fromMask;

		@SerializedName("prop2")
		private String prop2;

		@SerializedName("img_url")
		private String imgUrl;

		@SerializedName("adjust")
		private BigDecimal adjust;

		@SerializedName("refund_num")
		private BigDecimal refundNum;

		@SerializedName("suite_no")
		private String suiteNo;

		@SerializedName("platform_id")
		private Integer platformId;

		@SerializedName("paid")
		private BigDecimal paid;

		@SerializedName("src_oid")
		private String srcOid;

		@SerializedName("spec_name")
		private String specName;

		@SerializedName("goods_type")
		private Integer goodsType;

		@SerializedName("share_amount")
		private BigDecimal shareAmount;

		@SerializedName("suite_num")
		private BigDecimal suiteNum;

		@SerializedName("platform_status")
		private Integer platformStatus;

		@SerializedName("share_price")
		private BigDecimal sharePrice;

		public BigDecimal getSuiteDiscount()
		{
			return suiteDiscount;
		}

		public void setSuiteDiscount(BigDecimal suiteDiscount)
		{
			this.suiteDiscount = suiteDiscount;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
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

		public String getSuiteName()
		{
			return suiteName;
		}

		public void setSuiteName(String suiteName)
		{
			this.suiteName = suiteName;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public String getApiSpecId()
		{
			return apiSpecId;
		}

		public void setApiSpecId(String apiSpecId)
		{
			this.apiSpecId = apiSpecId;
		}

		public BigDecimal getSuiteAmount()
		{
			return suiteAmount;
		}

		public void setSuiteAmount(BigDecimal suiteAmount)
		{
			this.suiteAmount = suiteAmount;
		}

		public String getApiSpecName()
		{
			return apiSpecName;
		}

		public void setApiSpecName(String apiSpecName)
		{
			this.apiSpecName = apiSpecName;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
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

		public String getApiGoodsName()
		{
			return apiGoodsName;
		}

		public void setApiGoodsName(String apiGoodsName)
		{
			this.apiGoodsName = apiGoodsName;
		}

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public String getApiGoodsId()
		{
			return apiGoodsId;
		}

		public void setApiGoodsId(String apiGoodsId)
		{
			this.apiGoodsId = apiGoodsId;
		}

		public BigDecimal getCommission()
		{
			return commission;
		}

		public void setCommission(BigDecimal commission)
		{
			this.commission = commission;
		}

		public Integer getGuaranteeMode()
		{
			return guaranteeMode;
		}

		public void setGuaranteeMode(Integer guaranteeMode)
		{
			this.guaranteeMode = guaranteeMode;
		}

		public String getSrcTid()
		{
			return srcTid;
		}

		public void setSrcTid(String srcTid)
		{
			this.srcTid = srcTid;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public Integer getRefundStatus()
		{
			return refundStatus;
		}

		public void setRefundStatus(Integer refundStatus)
		{
			this.refundStatus = refundStatus;
		}

		public Integer getDeliveryTerm()
		{
			return deliveryTerm;
		}

		public void setDeliveryTerm(Integer deliveryTerm)
		{
			this.deliveryTerm = deliveryTerm;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
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

		public Integer getGiftType()
		{
			return giftType;
		}

		public void setGiftType(Integer giftType)
		{
			this.giftType = giftType;
		}

		public BigDecimal getOrderPrice()
		{
			return orderPrice;
		}

		public void setOrderPrice(BigDecimal orderPrice)
		{
			this.orderPrice = orderPrice;
		}

		public Integer getFromMask()
		{
			return fromMask;
		}

		public void setFromMask(Integer fromMask)
		{
			this.fromMask = fromMask;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getImgUrl()
		{
			return imgUrl;
		}

		public void setImgUrl(String imgUrl)
		{
			this.imgUrl = imgUrl;
		}

		public BigDecimal getAdjust()
		{
			return adjust;
		}

		public void setAdjust(BigDecimal adjust)
		{
			this.adjust = adjust;
		}

		public BigDecimal getRefundNum()
		{
			return refundNum;
		}

		public void setRefundNum(BigDecimal refundNum)
		{
			this.refundNum = refundNum;
		}

		public String getSuiteNo()
		{
			return suiteNo;
		}

		public void setSuiteNo(String suiteNo)
		{
			this.suiteNo = suiteNo;
		}

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public BigDecimal getPaid()
		{
			return paid;
		}

		public void setPaid(BigDecimal paid)
		{
			this.paid = paid;
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

		public BigDecimal getSuiteNum()
		{
			return suiteNum;
		}

		public void setSuiteNum(BigDecimal suiteNum)
		{
			this.suiteNum = suiteNum;
		}

		public Integer getPlatformStatus()
		{
			return platformStatus;
		}

		public void setPlatformStatus(Integer platformStatus)
		{
			this.platformStatus = platformStatus;
		}

		public BigDecimal getSharePrice()
		{
			return sharePrice;
		}

		public void setSharePrice(BigDecimal sharePrice)
		{
			this.sharePrice = sharePrice;
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