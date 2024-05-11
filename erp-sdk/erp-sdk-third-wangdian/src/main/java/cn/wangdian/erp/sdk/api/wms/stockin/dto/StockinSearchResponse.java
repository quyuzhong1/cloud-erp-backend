package cn.wangdian.erp.sdk.api.wms.stockin.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class StockinSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("order_list")
	private List<StockinSearchResponse.Order> orderList;

	public static class Order
	{
		private String checkTime ;
		private String created ;
		private Integer flagId ;
		private BigDecimal goodsCount;
		private Short goodsTypeCount ;
		private Integer logisticsId;
		private String logisticsNo ;
		private String modified ;
		private Short noteCount ;
		private Integer operatorId;
		private String remark ;
		private String srcOrderNo ;
		private Integer srcOrderId ;
		private Byte srcOrderType ;
		private Byte status ;
		private Integer stockinId ;
		private String stockinNo ;
		private String warehouseNo ;
		private String warehouseName ;
		private String operatorName ;
		private Short logisticsType ;
		@SerializedName("detail_list")
		private List<Detail> detailList ;

		public String getCheckTime()
		{
			return checkTime;
		}

		public void setCheckTime(String checkTime)
		{
			this.checkTime = checkTime;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public Integer getFlagId()
		{
			return flagId;
		}

		public void setFlagId(Integer flagId)
		{
			this.flagId = flagId;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Short getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(Short goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
		}

		public Integer getLogisticsId()
		{
			return logisticsId;
		}

		public void setLogisticsId(Integer logisticsId)
		{
			this.logisticsId = logisticsId;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public Short getNoteCount()
		{
			return noteCount;
		}

		public void setNoteCount(Short noteCount)
		{
			this.noteCount = noteCount;
		}

		public Integer getOperatorId()
		{
			return operatorId;
		}

		public void setOperatorId(Integer operatorId)
		{
			this.operatorId = operatorId;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getSrcOrderNo()
		{
			return srcOrderNo;
		}

		public void setSrcOrderNo(String srcOrderNo)
		{
			this.srcOrderNo = srcOrderNo;
		}

		public Integer getSrcOrderId()
		{
			return srcOrderId;
		}

		public void setSrcOrderId(Integer srcOrderId)
		{
			this.srcOrderId = srcOrderId;
		}

		public Byte getSrcOrderType()
		{
			return srcOrderType;
		}

		public void setSrcOrderType(Byte srcOrderType)
		{
			this.srcOrderType = srcOrderType;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public Integer getStockinId()
		{
			return stockinId;
		}

		public void setStockinId(Integer stockinId)
		{
			this.stockinId = stockinId;
		}

		public String getStockinNo()
		{
			return stockinNo;
		}

		public void setStockinNo(String stockinNo)
		{
			this.stockinNo = stockinNo;
		}

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

		public String getOperatorName()
		{
			return operatorName;
		}

		public void setOperatorName(String operatorName)
		{
			this.operatorName = operatorName;
		}

		public Short getLogisticsType()
		{
			return logisticsType;
		}

		public void setLogisticsType(Short logisticsType)
		{
			this.logisticsType = logisticsType;
		}

		public List<Detail> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<Detail> detailList)
		{
			this.detailList = detailList;
		}
	}

	public static class Detail
	{
		private BigDecimal adjustNum ;
		private Short baseUnitId ;
		private Integer batchId ;
		private String brandName ;
		private String brandNo ;
		private String created ;
		private BigDecimal expectNum ;
		private String expireDate ;
		private String goodsName ;
		private String goodsNo ;
		private String goodsUnitName ;
		private String modified ;
		private BigDecimal num ;
		private BigDecimal num2 ;
		private Integer orgStockinDetailId ;
		private Integer positionId ;
		private String productionDate ;
		private Integer recId ;
		private String remark ;
		private String specCode ;
		private String specNo ;
		private Integer specId ;
		private String specName ;
		private Integer stockinId ;
		private Short unitId ;
		private BigDecimal unitRatio ;
		private Short validityDays ;

		public BigDecimal getAdjustNum()
		{
			return adjustNum;
		}

		public void setAdjustNum(BigDecimal adjustNum)
		{
			this.adjustNum = adjustNum;
		}

		public Short getBaseUnitId()
		{
			return baseUnitId;
		}

		public void setBaseUnitId(Short baseUnitId)
		{
			this.baseUnitId = baseUnitId;
		}

		public Integer getBatchId()
		{
			return batchId;
		}

		public void setBatchId(Integer batchId)
		{
			this.batchId = batchId;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public String getBrandNo()
		{
			return brandNo;
		}

		public void setBrandNo(String brandNo)
		{
			this.brandNo = brandNo;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public BigDecimal getExpectNum()
		{
			return expectNum;
		}

		public void setExpectNum(BigDecimal expectNum)
		{
			this.expectNum = expectNum;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
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

		public String getGoodsUnitName()
		{
			return goodsUnitName;
		}

		public void setGoodsUnitName(String goodsUnitName)
		{
			this.goodsUnitName = goodsUnitName;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getNum2()
		{
			return num2;
		}

		public void setNum2(BigDecimal num2)
		{
			this.num2 = num2;
		}

		public Integer getOrgStockinDetailId()
		{
			return orgStockinDetailId;
		}

		public void setOrgStockinDetailId(Integer orgStockinDetailId)
		{
			this.orgStockinDetailId = orgStockinDetailId;
		}

		public Integer getPositionId()
		{
			return positionId;
		}

		public void setPositionId(Integer positionId)
		{
			this.positionId = positionId;
		}

		public String getProductionDate()
		{
			return productionDate;
		}

		public void setProductionDate(String productionDate)
		{
			this.productionDate = productionDate;
		}

		public Integer getRecId()
		{
			return recId;
		}

		public void setRecId(Integer recId)
		{
			this.recId = recId;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public Integer getSpecId()
		{
			return specId;
		}

		public void setSpecId(Integer specId)
		{
			this.specId = specId;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public Integer getStockinId()
		{
			return stockinId;
		}

		public void setStockinId(Integer stockinId)
		{
			this.stockinId = stockinId;
		}

		public Short getUnitId()
		{
			return unitId;
		}

		public void setUnitId(Short unitId)
		{
			this.unitId = unitId;
		}

		public BigDecimal getUnitRatio()
		{
			return unitRatio;
		}

		public void setUnitRatio(BigDecimal unitRatio)
		{
			this.unitRatio = unitRatio;
		}

		public Short getValidityDays()
		{
			return validityDays;
		}

		public void setValidityDays(Short validityDays)
		{
			this.validityDays = validityDays;
		}
	}
	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<Order> getOrderList()
	{
		return orderList;
	}

	public void setOrderList(List<Order> orderList)
	{
		this.orderList = orderList;
	}
}
