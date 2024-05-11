package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

public class WarehouseQueryRequest
{

	public static final byte TYPE_INNER = 1; // 普通仓库
	public static final byte TYPE_AUTO_FLOW = 2; // 自流转
	public static final byte TYPE_QIMEN = 3; // 奇门

	public static final short SUB_TYPE_DEFAULT = 0; // 默认
	public static final short SUB_TYPE_WDT = 1; // 旺店通
	public static final short SUB_TYPE_CAINIAO = 2; // 菜鸟
	public static final short SUB_TYPE_BS = 3; // 百世wms
	public static final short SUB_TYPE_JW = 4; // 巨沃WMS
	public static final short SUB_TYPE_XY = 5; // 心怡WMS
	public static final short SUB_TYPE_KJ = 6; // 科捷
	public static final short SUB_TYPE_JKY = 7; // 吉客云WMS
	public static final short SUB_TYPE_ZT = 8; // 中通wms
	public static final short SUB_TYPE_TTX = 9; // 通天晓
	public static final short SUB_TYPE_KCB = 10;// 酷仓宝WMS
	public static final short SUB_TYPE_JT = 11; // 景天WMS

	private String warehouseNo;
	private String warehouseName;
	private Byte type;
	private Short subType;
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getWarehouseName()
	{
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName)
	{
		this.warehouseName = warehouseName;
	}

	public Byte getType()
	{
		return type;
	}

	public void setType(Byte type)
	{
		this.type = type;
	}

	public Short getSubType()
	{
		return subType;
	}

	public void setSubType(Short subType)
	{
		this.subType = subType;
	}

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
}
