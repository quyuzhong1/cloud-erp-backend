package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

public class TradeQueryRequest
{
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;
	@SerializedName("warehouse_no")
	private String warehouseNo;
	@SerializedName("status")
	private String status;
	@SerializedName("trade_no")
	private String tradeNo;
	@SerializedName("shop_no")
	private String shopNo;
	@SerializedName("logistics_no")
	private String logisticsNo;
	@SerializedName("src_tid")
	private String srcTid;
	@SerializedName("status_type")
	private Integer statusType;

	public static final byte STATUS_OFFLINE_REFUND = 4; // 线下退款
	public static final byte STATUS_CANCEL = 5; // 已取消
	public static final byte STATUS_TO_PRECHECK = 6;// 待确认，待转预订单
	public static final byte STATUS_TO_COMPLETED = 7; // 待确认订单
	public static final byte STATUS_WAIT_TO_PAY = 10; // 待付款
	public static final byte STATUS_PART_PAID = 12; // 待尾款
	public static final byte STATUS_WAIT_UNPAY = 15; // 等未付
	public static final byte STATUS_DELAY_TO_CHECK = 16; // 延时审核
	public static final byte STATUS_PREV_PREORDER = 19; // 预订单前处理
	public static final byte STATUS_PREV_CHECK = 20; // 前处理(赠品，合并，拆分)
	public static final byte STATUS_EXCEPTION = 23; // 异常预订单
	public static final byte STATUS_PREORDER_EXCHANGE = 24; // 换货预订单
	public static final byte STATUS_PREORDER_HANDLING = 25; // 待处理预订单
	public static final byte STATUS_PREORDER_ACTIVATING = 26; // 待激活预订单
	public static final byte STATUS_PREORDER_QUEUE = 27; // 待分配预订单
	public static final byte STATUS_WAIT_CHECK = 30; // 待审核
	public static final byte STATUS_WAIT_FINCHECK = 35; // 待财审
	public static final byte STATUS_WMS_CONFIRMED = 55; // 已确认,已生成出库单
	public static final byte STATUS_CONSIGNED = 95; // 已发货
	public static final byte STATUS_NO_PLANNED_COST = 96;// 待录入计划成本，订单结算时有货品无计划成本
	public static final byte STATUS_POSTED = 101;// 已过账
	public static final String STATUS_COMPLETE = "110"; // 已完成

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndTime()
	{
		return endTime;
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getTradeNo()
	{
		return tradeNo;
	}

	public void setTradeNo(String tradeNo)
	{
		this.tradeNo = tradeNo;
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

	public String getSrcTid()
	{
		return srcTid;
	}

	public void setSrcTid(String srcTid)
	{
		this.srcTid = srcTid;
	}

	public Integer getStatusType() {
		return statusType;
	}

	public void setStatusType(Integer statusType) {
		this.statusType = statusType;
	}
}
