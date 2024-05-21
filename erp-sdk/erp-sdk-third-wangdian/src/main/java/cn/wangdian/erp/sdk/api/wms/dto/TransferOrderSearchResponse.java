package cn.wangdian.erp.sdk.api.wms.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class TransferOrderSearchResponse
{
	private int totalCount ;
	@SerializedName("order")
	private List<OrderItem> orderItemList ;

	public static class OrderItem
	{
		private BigDecimal goodsOutCount ;
		private BigDecimal goodsInCount ;
		private int goodsTypeCount ;
		private String remark ;
		private Long created ;
		private int transferId ;
		private BigDecimal goodsCount ;
		private String telno ;
		private String fromWarehouseNo ;
		private int mode ;
		private String transferNo ;
		private String concat ;
		private long modified ;
		private String creatorName ;
		private int status ;
		private String toWarehouseNo ;

		@SerializedName("details_list")
		private List<DetailItem> detailItemList ;

		public BigDecimal getGoodsOutCount()
		{
			return goodsOutCount;
		}

		public void setGoodsOutCount(BigDecimal goodsOutCount)
		{
			this.goodsOutCount = goodsOutCount;
		}

		public BigDecimal getGoodsInCount()
		{
			return goodsInCount;
		}

		public void setGoodsInCount(BigDecimal goodsInCount)
		{
			this.goodsInCount = goodsInCount;
		}

		public int getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(int goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Long getCreated()
		{
			return created;
		}

		public void setCreated(Long created)
		{
			this.created = created;
		}

		public int getTransferId()
		{
			return transferId;
		}

		public void setTransferId(int transferId)
		{
			this.transferId = transferId;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public String getTelno()
		{
			return telno;
		}

		public void setTelno(String telno)
		{
			this.telno = telno;
		}

		public String getFromWarehouseNo()
		{
			return fromWarehouseNo;
		}

		public void setFromWarehouseNo(String fromWarehouseNo)
		{
			this.fromWarehouseNo = fromWarehouseNo;
		}

		public int getMode()
		{
			return mode;
		}

		public void setMode(int mode)
		{
			this.mode = mode;
		}

		public String getTransferNo()
		{
			return transferNo;
		}

		public void setTransferNo(String transferNo)
		{
			this.transferNo = transferNo;
		}

		public String getConcat()
		{
			return concat;
		}

		public void setConcat(String concat)
		{
			this.concat = concat;
		}

		public long getModified()
		{
			return modified;
		}

		public void setModified(long modified)
		{
			this.modified = modified;
		}

		public String getCreatorName()
		{
			return creatorName;
		}

		public void setCreatorName(String creatorName)
		{
			this.creatorName = creatorName;
		}

		public int getStatus()
		{
			return status;
		}

		public void setStatus(int status)
		{
			this.status = status;
		}

		public String getToWarehouseNo()
		{
			return toWarehouseNo;
		}

		public void setToWarehouseNo(String toWarehouseNo)
		{
			this.toWarehouseNo = toWarehouseNo;
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

	public List<OrderItem> getOrderItemList()
	{
		return orderItemList;
	}

	public void setOrderItemList(List<OrderItem> orderItemList)
	{
		this.orderItemList = orderItemList;
	}

	public static class DetailItem
	{
		private int transferId ;
		private int batchId ;
		private String batchNo ;
		private BigDecimal stockNum ;
		private BigDecimal num ;
		private BigDecimal outNum ;
		private BigDecimal inNum ;
		private BigDecimal auxNum ;
		private String remark ;
		private String specNo ;
		private String specCode ;
		private String specName ;
		private String barcode ;
		private String toPosition ;
		private String unitName ;
		private String auxUnitName ;

		public int getTransferId()
		{
			return transferId;
		}

		public void setTransferId(int transferId)
		{
			this.transferId = transferId;
		}

		public int getBatchId()
		{
			return batchId;
		}

		public void setBatchId(int batchId)
		{
			this.batchId = batchId;
		}

		public String getBatchNo()
		{
			return batchNo;
		}

		public void setBatchNo(String batchNo)
		{
			this.batchNo = batchNo;
		}

		public BigDecimal getStockNum()
		{
			return stockNum;
		}

		public void setStockNum(BigDecimal stockNum)
		{
			this.stockNum = stockNum;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getOutNum()
		{
			return outNum;
		}

		public void setOutNum(BigDecimal outNum)
		{
			this.outNum = outNum;
		}

		public BigDecimal getInNum()
		{
			return inNum;
		}

		public void setInNum(BigDecimal inNum)
		{
			this.inNum = inNum;
		}

		public BigDecimal getAuxNum()
		{
			return auxNum;
		}

		public void setAuxNum(BigDecimal auxNum)
		{
			this.auxNum = auxNum;
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

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getToPosition()
		{
			return toPosition;
		}

		public void setToPosition(String toPosition)
		{
			this.toPosition = toPosition;
		}

		public String getUnitName()
		{
			return unitName;
		}

		public void setUnitName(String unitName)
		{
			this.unitName = unitName;
		}

		public String getAuxUnitName()
		{
			return auxUnitName;
		}

		public void setAuxUnitName(String auxUnitName)
		{
			this.auxUnitName = auxUnitName;
		}
	}
}
