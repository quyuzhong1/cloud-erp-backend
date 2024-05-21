package cn.wangdian.erp.sdk.api.aftersales.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class RefundSearchResponse
{
	/*
	{ "status": 0, "data": { "total_count": 170, "order": [{ "tid_list":
	"P32779733707900", "detail_list": [{ "platform_id": 19, "oid":
	"vpubaoP32779733707900:1486", "tid": "P32779733707900", "trade_no":
	"JY201903270026", "num": 1.0000, "price": 0.0100, "original_price":
	0.0100, "refund_num": 1.0000, "total_amount": 0.0100, "refund_amount":
	0.0100, "is_guarantee": true, "goods_no": "all", "goods_name": "阿拉蕾",
	"spec_name": "颜色:蓝色", "spec_no": "all1", "barcode": "all1",
	"stockin_num": 0.0000, "remark": "", "api_goods_name": "阿拉蕾",
	"api_spec_name": "阿拉蕾:颜色：粉红黄条纹" }], "refund_no": "TK1903270003",
	"remark": "", "type": 1, "stockin_status": 0, "flag_name": "无",
	"return_goods_count": 1.0000, "receiver_telno": "15201460155",
	"receiver_name": "刘子渲", "modified": 1553680649000, "note_count": 0,
	"shop_no": "weipubao", "from_type": 0, "created": 1553680627000,
	"return_logistics_no": "", "trade_no_list": "JY201903270026",
	"guarantee_refund_amount": 0.0100, "return_goods_amount": 0.0100,
	"return_logistics_name": "", "buyer_nick": "Vpubao15201460155",
	"operator_name": "刘子渲", "actual_refund_amount": 0.0100,
	"return_warehouse_no": "", "direct_refund_amount": 0.0000,
	"receive_amount": 0.0000, "customer_name": "系统", "reason_name": "无",
	"status": 10, "revert_reason_name": "无" }] } }
	 */

	@SerializedName("total_count")
	private Integer total;
	@SerializedName("order")
	private List<RefundOrderDto> orders;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<RefundOrderDto> getOrders()
	{
		return orders;
	}

	public void setOrders(List<RefundOrderDto> orders)
	{
		this.orders = orders;
	}

	public static class RefundOrderDto
	{

		/**
		 * 退款(未发货,退款申请)
		 */
		public static final Byte REFUND_TYPE_REFUND = 1;
		/**
		 * 退货
		 */
		public static final Byte REFUND_TYPE_RETURN_GOODS = 2;
		/**
		 * 换货
		 */
		public static final Byte REFUND_TYPE_SWAP_GOODS = 3;
		/**
		 * 退款不退货
		 */
		public static final Byte REFUND_TYPE_REFUND_NOT_RETURN = 4;

		/**
		 * 未入库
		 */
		public static final Byte STOCKIN_STATUS_NONE = 0;
		/**
		 * 待入库
		 */
		public static final Byte STOCKIN_STATUS_WAIT = 1;
		/**
		 * 部分入库
		 */
		public static final Byte STOCKIN_STATUS_PART = 2;
		/**
		 * 全部入库
		 */
		public static final Byte STOCKIN_STATUS_FULL = 3;
		/**
		 * 终止入库,停止等待
		 */
		public static final Byte STOCKIN_STATUS_DISCARD = 4;

		/**
		 * 10 已取消
		 */
		public static final Byte STATUS_CANCEL = 10; // 已取消
		/**
		 * 20 待审核
		 */
		public static final Byte STATUS_WAIT_CHECK = 20; // 待审核
		/**
		 * 30 已审核
		 */
		public static final Byte STATUS_AGREE = 30; // 已审核
		/**
		 * 80 已结算
		 */
		public static final Byte STATUS_SETTLED = 80; // 已结算
		/**
		 * 85 待过账
		 */
		public static final Byte STATUS_WAIT_POST = 85; // 待过账
		/**
		 * 86 已过账
		 */
		public static final Byte STATUS_POSTED = 86; // 已过账
		/**
		 * 87 成本确认
		 */
		public static final Byte STATUS_NO_PLANNED_COST = 87; // 成本确认
		/**
		 * 90 已完成
		 */
		public static final Byte STATUS_COMPLETE = 90; // 已完成

		private String tidList;
		private String refundNo;
		private String remark;
		private Byte type;
		private Byte stockinStatus;
		private String flagName;
		private BigDecimal returnGoodsCount;
		private String receiverTelno;
		private String receiverName;
		private String modified;
		private BigDecimal noteCount;
		private String shopNo;
		private Byte fromType;
		private String created;
		private String returnLogisticsNo;
		private String tradeNoList;
		private BigDecimal guaranteeRefundAmount;
		private BigDecimal returnGoodsAmount;
		private String returnLogisticsName;
		private String buyerNick;
		private String operatorName;
		private BigDecimal actualRefundAmount;
		private String returnWarehouseNo;
		private BigDecimal directRefundAmount;
		private BigDecimal receiveAmount;
		private String customerName;
		private String reasonName;
		private Byte status;
		private String revertReasonName;
		private String swapOutTradeNo;
		private String refundTime;
		private List<RefundOrderInfoDto> detailList;
		private List<SwapOrderInfoDto> swapOutDetailList;

		public String getTidList()
		{
			return tidList;
		}

		public void setTidList(String tidList)
		{
			this.tidList = tidList;
		}

		public String getRefundNo()
		{
			return refundNo;
		}

		public void setRefundNo(String refundNo)
		{
			this.refundNo = refundNo;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Byte getType()
		{
			return type;
		}

		public void setType(Byte type)
		{
			this.type = type;
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

		public BigDecimal getReturnGoodsCount()
		{
			return returnGoodsCount;
		}

		public void setReturnGoodsCount(BigDecimal returnGoodsCount)
		{
			this.returnGoodsCount = returnGoodsCount;
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

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public BigDecimal getNoteCount()
		{
			return noteCount;
		}

		public void setNoteCount(BigDecimal noteCount)
		{
			this.noteCount = noteCount;
		}

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public Byte getFromType()
		{
			return fromType;
		}

		public void setFromType(Byte fromType)
		{
			this.fromType = fromType;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getReturnLogisticsNo()
		{
			return returnLogisticsNo;
		}

		public void setReturnLogisticsNo(String returnLogisticsNo)
		{
			this.returnLogisticsNo = returnLogisticsNo;
		}

		public String getTradeNoList()
		{
			return tradeNoList;
		}

		public void setTradeNoList(String tradeNoList)
		{
			this.tradeNoList = tradeNoList;
		}

		public BigDecimal getGuaranteeRefundAmount()
		{
			return guaranteeRefundAmount;
		}

		public void setGuaranteeRefundAmount(BigDecimal guaranteeRefundAmount)
		{
			this.guaranteeRefundAmount = guaranteeRefundAmount;
		}

		public BigDecimal getReturnGoodsAmount()
		{
			return returnGoodsAmount;
		}

		public void setReturnGoodsAmount(BigDecimal returnGoodsAmount)
		{
			this.returnGoodsAmount = returnGoodsAmount;
		}

		public String getReturnLogisticsName()
		{
			return returnLogisticsName;
		}

		public void setReturnLogisticsName(String returnLogisticsName)
		{
			this.returnLogisticsName = returnLogisticsName;
		}

		public String getBuyerNick()
		{
			return buyerNick;
		}

		public void setBuyerNick(String buyerNick)
		{
			this.buyerNick = buyerNick;
		}

		public String getOperatorName()
		{
			return operatorName;
		}

		public void setOperatorName(String operatorName)
		{
			this.operatorName = operatorName;
		}

		public BigDecimal getActualRefundAmount()
		{
			return actualRefundAmount;
		}

		public void setActualRefundAmount(BigDecimal actualRefundAmount)
		{
			this.actualRefundAmount = actualRefundAmount;
		}

		public String getReturnWarehouseNo()
		{
			return returnWarehouseNo;
		}

		public void setReturnWarehouseNo(String returnWarehouseNo)
		{
			this.returnWarehouseNo = returnWarehouseNo;
		}

		public BigDecimal getDirectRefundAmount()
		{
			return directRefundAmount;
		}

		public void setDirectRefundAmount(BigDecimal directRefundAmount)
		{
			this.directRefundAmount = directRefundAmount;
		}

		public BigDecimal getReceiveAmount()
		{
			return receiveAmount;
		}

		public void setReceiveAmount(BigDecimal receiveAmount)
		{
			this.receiveAmount = receiveAmount;
		}

		public String getCustomerName()
		{
			return customerName;
		}

		public void setCustomerName(String customerName)
		{
			this.customerName = customerName;
		}

		public String getReasonName()
		{
			return reasonName;
		}

		public void setReasonName(String reasonName)
		{
			this.reasonName = reasonName;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public String getRevertReasonName()
		{
			return revertReasonName;
		}

		public void setRevertReasonName(String revertReasonName)
		{
			this.revertReasonName = revertReasonName;
		}

		public List<RefundOrderInfoDto> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<RefundOrderInfoDto> detailList)
		{
			this.detailList = detailList;
		}

		public String getSwapOutTradeNo()
		{
			return swapOutTradeNo;
		}

		public void setSwapOutTradeNo(String swapOutTradeNo)
		{
			this.swapOutTradeNo = swapOutTradeNo;
		}

		public List<SwapOrderInfoDto> getSwapOutDetailList()
		{
			return swapOutDetailList;
		}

		public void setSwapOutDetailList(List<SwapOrderInfoDto> swapOutDetailList)
		{
			this.swapOutDetailList = swapOutDetailList;
		}

		public String getRefundTime()
		{
			return refundTime;
		}

		public void setRefundTime(String refundTime)
		{
			this.refundTime = refundTime;
		}
	}

	public static class RefundOrderInfoDto
	{

		private Integer platformId;
		private String oid;
		private String tid;
		private String tradeNo;
		private BigDecimal num;
		private BigDecimal price;
		private BigDecimal originalPrice;
		private BigDecimal refundNum;
		private BigDecimal totalAmount;
		private BigDecimal refundAmount;
		@SerializedName("is_guarantee")
		private boolean guarantee;
		private String goodsNo;
		private String goodsName;
		private String specName;
		private String specNo;
		private String barcode;
		private BigDecimal stockinNum;
		private String remark;
		private String apiGoodsName;
		private String apiSpecName;

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public String getOid()
		{
			return oid;
		}

		public void setOid(String oid)
		{
			this.oid = oid;
		}

		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public String getTradeNo()
		{
			return tradeNo;
		}

		public void setTradeNo(String tradeNo)
		{
			this.tradeNo = tradeNo;
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

		public BigDecimal getOriginalPrice()
		{
			return originalPrice;
		}

		public void setOriginalPrice(BigDecimal originalPrice)
		{
			this.originalPrice = originalPrice;
		}

		public BigDecimal getRefundNum()
		{
			return refundNum;
		}

		public void setRefundNum(BigDecimal refundNum)
		{
			this.refundNum = refundNum;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
		}

		public BigDecimal getRefundAmount()
		{
			return refundAmount;
		}

		public void setRefundAmount(BigDecimal refundAmount)
		{
			this.refundAmount = refundAmount;
		}

		public boolean isGuarantee()
		{
			return guarantee;
		}

		public void setGuarantee(boolean guarantee)
		{
			this.guarantee = guarantee;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
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

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public BigDecimal getStockinNum()
		{
			return stockinNum;
		}

		public void setStockinNum(BigDecimal stockinNum)
		{
			this.stockinNum = stockinNum;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getApiGoodsName()
		{
			return apiGoodsName;
		}

		public void setApiGoodsName(String apiGoodsName)
		{
			this.apiGoodsName = apiGoodsName;
		}

		public String getApiSpecName()
		{
			return apiSpecName;
		}

		public void setApiSpecName(String apiSpecName)
		{
			this.apiSpecName = apiSpecName;
		}
	}

	public static class SwapOrderInfoDto
	{

		private Byte targetType;
		private int targetId;
		private boolean defect;
		private String goodsName;
		private String goodsNo;
		private String specName;
		private String specCode;
		private String merchantNo;
		private BigDecimal price;
		private BigDecimal totalAmount;
		private BigDecimal num;
		private String remark;
		private String oid;

		public Byte getTargetType()
		{
			return targetType;
		}

		public void setTargetType(Byte targetType)
		{
			this.targetType = targetType;
		}

		public int getTargetId()
		{
			return targetId;
		}

		public void setTargetId(int targetId)
		{
			this.targetId = targetId;
		}

		public boolean isDefect()
		{
			return defect;
		}

		public void setDefect(boolean defect)
		{
			this.defect = defect;
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

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getMerchantNo()
		{
			return merchantNo;
		}

		public void setMerchantNo(String merchantNo)
		{
			this.merchantNo = merchantNo;
		}

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
		}

		public BigDecimal getTotalAmount()
		{
			return totalAmount;
		}

		public void setTotalAmount(BigDecimal totalAmount)
		{
			this.totalAmount = totalAmount;
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

		public String getOid()
		{
			return oid;
		}

		public void setOid(String oid)
		{
			this.oid = oid;
		}
	}
}
