package cn.wangdian.erp.sdk.api.aftersales.dto;

public class RefundSearchRequest
{

	public static final Byte STATUS_CANCEL = 10; // 已取消
	public static final Byte STATUS_WAIT_CHECK = 20; // 待审核
	public static final Byte STATUS_AGREE = 30; // 已审核
	public static final Byte STATUS_SETTLED = 80; // 已结算
	public static final Byte STATUS_WAIT_POST = 85; // 待过账
	public static final Byte STATUS_POSTED = 86; // 已过账
	public static final Byte STATUS_NO_PLANNED_COST = 87; // 成本确认
	public static final Byte STATUS_COMPLETE = 90; // 已完成

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

	private String shopNos;
	private String tid;
	private String buyerNick;
	private String tradeNo;
	private String refundNo;
	private String modifiedFrom;
	private String modifiedTo;
	private Byte status;
	private Byte stockinStatus;
	private Byte type;

	public String getShopNos()
	{
		return shopNos;
	}

	public void setShopNos(String shopNos)
	{
		this.shopNos = shopNos;
	}

	public String getTid()
	{
		return tid;
	}

	public void setTid(String tid)
	{
		this.tid = tid;
	}

	public String getBuyerNick()
	{
		return buyerNick;
	}

	public void setBuyerNick(String buyerNick)
	{
		this.buyerNick = buyerNick;
	}

	public String getTradeNo()
	{
		return tradeNo;
	}

	public void setTradeNo(String tradeNo)
	{
		this.tradeNo = tradeNo;
	}

	public String getRefundNo()
	{
		return refundNo;
	}

	public void setRefundNo(String refundNo)
	{
		this.refundNo = refundNo;
	}

	public String getModifiedFrom()
	{
		return modifiedFrom;
	}

	public void setModifiedFrom(String modifiedFrom)
	{
		this.modifiedFrom = modifiedFrom;
	}

	public String getModifiedTo()
	{
		return modifiedTo;
	}

	public void setModifiedTo(String modifiedTo)
	{
		this.modifiedTo = modifiedTo;
	}

	public Byte getStatus()
	{
		return status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
	}

	public Byte getStockinStatus()
	{
		return stockinStatus;
	}

	public void setStockinStatus(Byte stockinStatus)
	{
		this.stockinStatus = stockinStatus;
	}

	public Byte getType()
	{
		return type;
	}

	public void setType(Byte type)
	{
		this.type = type;
	}
}
