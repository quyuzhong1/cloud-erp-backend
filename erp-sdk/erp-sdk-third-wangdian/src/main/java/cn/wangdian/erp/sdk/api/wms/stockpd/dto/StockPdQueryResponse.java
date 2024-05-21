package cn.wangdian.erp.sdk.api.wms.stockpd.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StockPdQueryResponse
{
	private int total;
	@SerializedName("data")
    private List<StockPdQueryResponse.Item> data;

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public static class Item
	{
		private int recId;
		private int warehouseId;

		@SerializedName("pd_no") private String orderNo;
		@SerializedName("warehouse_no") private String warehouseNo;
		private int mode;

		@SerializedName("remark") private String remark;
		private String operatorNo;
		private String creatorNo;
		private String created;
		private String modified;

		@SerializedName("status") private Byte status;

		public int getRecId()
		{
			return recId;
		}

		public void setRecId(int recId)
		{
			this.recId = recId;
		}

		public int getWarehouseId()
		{
			return warehouseId;
		}

		public void setWarehouseId(int warehouseId)
		{
			this.warehouseId = warehouseId;
		}

		public String getOrderNo()
		{
			return orderNo;
		}

		public void setOrderNo(String orderNo)
		{
			this.orderNo = orderNo;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public int getMode()
		{
			return mode;
		}

		public void setMode(int mode)
		{
			this.mode = mode;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getOperatorNo()
		{
			return operatorNo;
		}

		public void setOperatorNo(String operatorNo)
		{
			this.operatorNo = operatorNo;
		}

		public String getCreatorNo()
		{
			return creatorNo;
		}

		public void setCreatorNo(String creatorNo)
		{
			this.creatorNo = creatorNo;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}
	}

	public List<Item> getData()
	{
		return data;
	}

	public void setData(List<Item> data)
	{
		this.data = data;
	}
}
