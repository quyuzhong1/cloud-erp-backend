package cn.wangdian.erp.sdk.api.wms.stockout.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class StockoutSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("order_list")
	private List<StockoutSearchResponse.Order> orderList;

	public static class Order
	{
		private BigDecimal calcPostCost ;
		private BigDecimal calcWeight ;
		private BigDecimal checkedGoodsTotalCost ;
		private Integer consignStatus ;
		private String consignTime;
		private String created ;
		private Integer customType ;
		private String errorInfo ;
		private String expireDate ;
		private BigDecimal goodsCount ;
		private Integer goodsTypeCount ;
		private String logisticsCompanyNo ;
		private Integer logisticsId ;
		private String logisticsName ;
		private String logisticsNo ;
		private Short logisticsType ;
		private String modified ;
		private String operatorName;
		private Integer receiverCity;
		private Integer receiverCountry;
		private Integer receiverDistrict;
		private Integer receiverProvince;
		private String receiverZip;
		private String remark;
		private Integer srcOrderId;
		private String srcOrderNo;
		private Byte srcOrderType;
		private Byte status;
		private Integer stockoutId;
		private String stockoutNo;
		private Short warehouseId;
		private String warehouseNo;
		private Byte warehouseType;
		private BigDecimal weightPostCost;
		private BigDecimal weight;
		@SerializedName("detail_list")
		private List<Detail> detailList;

		public BigDecimal getCalcPostCost()
		{
			return calcPostCost;
		}

		public void setCalcPostCost(BigDecimal calcPostCost)
		{
			this.calcPostCost = calcPostCost;
		}

		public BigDecimal getCalcWeight()
		{
			return calcWeight;
		}

		public void setCalcWeight(BigDecimal calcWeight)
		{
			this.calcWeight = calcWeight;
		}

		public BigDecimal getCheckedGoodsTotalCost()
		{
			return checkedGoodsTotalCost;
		}

		public void setCheckedGoodsTotalCost(BigDecimal checkedGoodsTotalCost)
		{
			this.checkedGoodsTotalCost = checkedGoodsTotalCost;
		}

		public Integer getConsignStatus()
		{
			return consignStatus;
		}

		public void setConsignStatus(Integer consignStatus)
		{
			this.consignStatus = consignStatus;
		}

		public String getConsignTime()
		{
			return consignTime;
		}

		public void setConsignTime(String consignTime)
		{
			this.consignTime = consignTime;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public Integer getCustomType()
		{
			return customType;
		}

		public void setCustomType(Integer customType)
		{
			this.customType = customType;
		}

		public String getErrorInfo()
		{
			return errorInfo;
		}

		public void setErrorInfo(String errorInfo)
		{
			this.errorInfo = errorInfo;
		}

		public String getExpireDate()
		{
			return expireDate;
		}

		public void setExpireDate(String expireDate)
		{
			this.expireDate = expireDate;
		}

		public BigDecimal getGoodsCount()
		{
			return goodsCount;
		}

		public void setGoodsCount(BigDecimal goodsCount)
		{
			this.goodsCount = goodsCount;
		}

		public Integer getGoodsTypeCount()
		{
			return goodsTypeCount;
		}

		public void setGoodsTypeCount(Integer goodsTypeCount)
		{
			this.goodsTypeCount = goodsTypeCount;
		}

		public String getLogisticsCompanyNo()
		{
			return logisticsCompanyNo;
		}

		public void setLogisticsCompanyNo(String logisticsCompanyNo)
		{
			this.logisticsCompanyNo = logisticsCompanyNo;
		}

		public Integer getLogisticsId()
		{
			return logisticsId;
		}

		public void setLogisticsId(Integer logisticsId)
		{
			this.logisticsId = logisticsId;
		}

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public Short getLogisticsType()
		{
			return logisticsType;
		}

		public void setLogisticsType(Short logisticsType)
		{
			this.logisticsType = logisticsType;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public String getOperatorName()
		{
			return operatorName;
		}

		public void setOperatorName(String operatorName)
		{
			this.operatorName = operatorName;
		}

		public Integer getReceiverCity()
		{
			return receiverCity;
		}

		public void setReceiverCity(Integer receiverCity)
		{
			this.receiverCity = receiverCity;
		}

		public Integer getReceiverCountry()
		{
			return receiverCountry;
		}

		public void setReceiverCountry(Integer receiverCountry)
		{
			this.receiverCountry = receiverCountry;
		}

		public Integer getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(Integer receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
		}

		public Integer getReceiverProvince()
		{
			return receiverProvince;
		}

		public void setReceiverProvince(Integer receiverProvince)
		{
			this.receiverProvince = receiverProvince;
		}

		public String getReceiverZip()
		{
			return receiverZip;
		}

		public void setReceiverZip(String receiverZip)
		{
			this.receiverZip = receiverZip;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Integer getSrcOrderId()
		{
			return srcOrderId;
		}

		public void setSrcOrderId(Integer srcOrderId)
		{
			this.srcOrderId = srcOrderId;
		}

		public String getSrcOrderNo()
		{
			return srcOrderNo;
		}

		public void setSrcOrderNo(String srcOrderNo)
		{
			this.srcOrderNo = srcOrderNo;
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

		public Integer getStockoutId()
		{
			return stockoutId;
		}

		public void setStockoutId(Integer stockoutId)
		{
			this.stockoutId = stockoutId;
		}

		public String getStockoutNo()
		{
			return stockoutNo;
		}

		public void setStockoutNo(String stockoutNo)
		{
			this.stockoutNo = stockoutNo;
		}

		public Short getWarehouseId()
		{
			return warehouseId;
		}

		public void setWarehouseId(Short warehouseId)
		{
			this.warehouseId = warehouseId;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public Byte getWarehouseType()
		{
			return warehouseType;
		}

		public void setWarehouseType(Byte warehouseType)
		{
			this.warehouseType = warehouseType;
		}

		public BigDecimal getWeightPostCost()
		{
			return weightPostCost;
		}

		public void setWeightPostCost(BigDecimal weightPostCost)
		{
			this.weightPostCost = weightPostCost;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
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
		private Integer batchId ;
		private String batchNo ;
		private String batchRemark;
		private String brandName;
		private String brandNo ;
		private BigDecimal checkedCostPrice ;
		private Integer goodsId;
		private String goodsName;
		private String goodsNo;
		private Byte goodsType;
		private Boolean isPackage;
		private BigDecimal num;
		private Integer positionId;
		private String positionNo;
		private Integer recId;
		private String remark;
		private Byte scanType;
		private String specCode;
		private Integer specId;
		private String specName;
		private String specNo;
		private Integer stockoutId;
		private String unit;
		private Short unitId;
		private BigDecimal weight ;

		public Integer getBatchId()
		{
			return batchId;
		}

		public void setBatchId(Integer batchId)
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

		public String getBatchRemark()
		{
			return batchRemark;
		}

		public void setBatchRemark(String batchRemark)
		{
			this.batchRemark = batchRemark;
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

		public BigDecimal getCheckedCostPrice()
		{
			return checkedCostPrice;
		}

		public void setCheckedCostPrice(BigDecimal checkedCostPrice)
		{
			this.checkedCostPrice = checkedCostPrice;
		}

		public Integer getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(Integer goodsId)
		{
			this.goodsId = goodsId;
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

		public Byte getGoodsType()
		{
			return goodsType;
		}

		public void setGoodsType(Byte goodsType)
		{
			this.goodsType = goodsType;
		}

		public Boolean getPackage()
		{
			return isPackage;
		}

		public void setPackage(Boolean aPackage)
		{
			isPackage = aPackage;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public Integer getPositionId()
		{
			return positionId;
		}

		public void setPositionId(Integer positionId)
		{
			this.positionId = positionId;
		}

		public String getPositionNo()
		{
			return positionNo;
		}

		public void setPositionNo(String positionNo)
		{
			this.positionNo = positionNo;
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

		public Byte getScanType()
		{
			return scanType;
		}

		public void setScanType(Byte scanType)
		{
			this.scanType = scanType;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
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

		public String getUnit()
		{
			return unit;
		}

		public void setUnit(String unit)
		{
			this.unit = unit;
		}

		public Short getUnitId()
		{
			return unitId;
		}

		public void setUnitId(Short unitId)
		{
			this.unitId = unitId;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
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
