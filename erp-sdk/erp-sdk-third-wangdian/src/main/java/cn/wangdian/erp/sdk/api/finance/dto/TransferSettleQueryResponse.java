package cn.wangdian.erp.sdk.api.finance.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class TransferSettleQueryResponse
{
	@SerializedName("total_count") private Integer totals;
	@SerializedName("order") private List<TransferSettleQueryResponse.OrderInfoDto> orderLists;

	public Integer getTotal()
	{
		return totals;
	}

	public void setTotal(Integer total)
	{
		this.totals = total;
	}

	public List<TransferSettleQueryResponse.OrderInfoDto> getOrderList()
	{
		return orderLists;
	}

	public void setOrderList(List<TransferSettleQueryResponse.OrderInfoDto> orderList)
	{
		this.orderLists = orderList;
	}

	public static class OrderInfoDto
	{
		private BigDecimal totalCost;
		private String checkerName;
		private BigDecimal postCost;
		private BigDecimal otherFee;
		private String created;
		private String transferNo;
		private String remark;
		private String settleNo;
		private String fromWarehouseNo;
		private String toWarehouseNo;
		private Integer status;
		private String settlerName;
		@SerializedName("details_list") private List<TransferSettleQueryResponse.OrderDetailInfoDto> detailList;

		public BigDecimal getTotalCost()
		{
			return totalCost;
		}

		public void setTotalCost(BigDecimal totalCost)
		{
			this.totalCost = totalCost;
		}

		public String getCheckerName()
		{
			return checkerName;
		}

		public void setCheckerName(String checkerName)
		{
			this.checkerName = checkerName;
		}

		public BigDecimal getPostCost()
		{
			return postCost;
		}

		public void setPostCost(BigDecimal postCost)
		{
			this.postCost = postCost;
		}

		public BigDecimal getOtherFee()
		{
			return otherFee;
		}

		public void setOtherFee(BigDecimal otherFee)
		{
			this.otherFee = otherFee;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getTransferNo()
		{
			return transferNo;
		}

		public void setTransferNo(String transferNo)
		{
			this.transferNo = transferNo;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getSettleNo()
		{
			return settleNo;
		}

		public void setSettleNo(String settleNo)
		{
			this.settleNo = settleNo;
		}

		public String getFromWarehouseNo()
		{
			return fromWarehouseNo;
		}

		public void setFromWarehouseNo(String fromWarehouseNo)
		{
			this.fromWarehouseNo = fromWarehouseNo;
		}

		public String getToWarehouseNo()
		{
			return toWarehouseNo;
		}

		public void setToWarehouseNo(String toWarehouseNo)
		{
			this.toWarehouseNo = toWarehouseNo;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
		}

		public String getSettlerName()
		{
			return settlerName;
		}

		public void setSettlerName(String settlerName)
		{
			this.settlerName = settlerName;
		}

		public List<OrderDetailInfoDto> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<OrderDetailInfoDto> detailList)
		{
			this.detailList = detailList;
		}
	}

	public static class OrderDetailInfoDto
	{
		private String specNo;
		private String goodsName;
		private String goods_no;
		private String specName;
		private String specCode;
		private String barcode;
		private Boolean defect;
		private BigDecimal num;
		private BigDecimal inNum;
		private BigDecimal outNum;
		private BigDecimal avgCost;
		private BigDecimal costPrice;
		private BigDecimal totalCost;
		private String remark;

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getGoods_no()
		{
			return goods_no;
		}

		public void setGoods_no(String goods_no)
		{
			this.goods_no = goods_no;
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

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public Boolean getDefect()
		{
			return defect;
		}

		public void setDefect(Boolean defect)
		{
			this.defect = defect;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getInNum()
		{
			return inNum;
		}

		public void setInNum(BigDecimal inNum)
		{
			this.inNum = inNum;
		}

		public BigDecimal getOutNum()
		{
			return outNum;
		}

		public void setOutNum(BigDecimal outNum)
		{
			this.outNum = outNum;
		}

		public BigDecimal getAvgCost()
		{
			return avgCost;
		}

		public void setAvgCost(BigDecimal avgCost)
		{
			this.avgCost = avgCost;
		}

		public BigDecimal getCostPrice()
		{
			return costPrice;
		}

		public void setCostPrice(BigDecimal costPrice)
		{
			this.costPrice = costPrice;
		}

		public BigDecimal getTotalCost()
		{
			return totalCost;
		}

		public void setTotalCost(BigDecimal totalCost)
		{
			this.totalCost = totalCost;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}
	}
}


