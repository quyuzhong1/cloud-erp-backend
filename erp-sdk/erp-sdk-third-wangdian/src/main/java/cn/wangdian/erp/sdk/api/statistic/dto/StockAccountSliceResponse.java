package cn.wangdian.erp.sdk.api.statistic.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class StockAccountSliceResponse
{
	@SerializedName("total_count") private Integer total;
	@SerializedName("detail_list") private List<StockAccountSliceResponse.Detail> detailList;

	public static class Detail
	{
		private Integer specId;
		private String specNo;
		private Integer goodsId;
		private String goodsNo;
		private String goodsName;
		private String shortName;
		private String specCode;
		private String specName;
		private String className;
		private String brandName;
		private boolean defect;
		private Integer warehouseId;
		private String warehouseName;
		private BigDecimal beginCost;
		private BigDecimal endCost;
		private BigDecimal beginStock;
		private BigDecimal beginCosttotal;
		private BigDecimal endStock;
		private BigDecimal endCosttotal;
		private BigDecimal stockinSettleAmount;
		private BigDecimal averageStock;
		private BigDecimal stockTurnover;
		private BigDecimal stockoutCount;
		private BigDecimal stockoutCost;
		private BigDecimal stockinCount;
		private BigDecimal stockinMoney;
		private BigDecimal sellCount;
		private BigDecimal sellCost;
		private BigDecimal sellMoney;
		private BigDecimal traOutCount;
		private BigDecimal traOutCost;
		private BigDecimal traOutMoney;
		private BigDecimal purBackCount;
		private BigDecimal purBackCost;
		private BigDecimal purBackMoney;
		private BigDecimal pdOutCount;
		private BigDecimal pdOutCost;
		private BigDecimal pdOutMoney;
		private BigDecimal otherOutCount;
		private BigDecimal otherOutCost;
		private BigDecimal otherOutMoney;
		private BigDecimal intOutCount;
		private BigDecimal intOutCost;
		private BigDecimal intOutMoney;
		private BigDecimal proOutCount;
		private BigDecimal proOutCost;
		private BigDecimal proOutMoney;
		private BigDecimal outerOutCount;
		private BigDecimal outerOutCost;
		private BigDecimal outerOutMoney;
		private BigDecimal defectOutCount;
		private BigDecimal defectOutCost;
		private BigDecimal defectOutMoney;
		private BigDecimal purInCount;
		private BigDecimal purInMoney;
		private BigDecimal traInCount;
		private BigDecimal traInMoney;
		private BigDecimal sellbackCount;
		private BigDecimal sellbackMoney;
		private BigDecimal pdInCount;
		private BigDecimal pdInMoney;
		private BigDecimal otherInCount;
		private BigDecimal otherInMoney;
		private BigDecimal intInCount;
		private BigDecimal intInMoney;
		private BigDecimal preInCount;
		private BigDecimal preInMoney;
		private BigDecimal proInCount;
		private BigDecimal proInMoney;
		private BigDecimal productInCount;
		private BigDecimal productInMoney;
		private BigDecimal outerInCount;
		private BigDecimal outerInMoney;
		private BigDecimal defectInCount;
		private BigDecimal defectInMoney;
		private String prop1;
		private String prop2;
		private String prop3;
		private String prop4;
		private String prop5;
		private String prop6;
		private String providerName;

		public Integer getSpecId()
		{
			return specId;
		}

		public void setSpecId(Integer specId)
		{
			this.specId = specId;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public Integer getGoodsId()
		{
			return goodsId;
		}

		public void setGoodsId(Integer goodsId)
		{
			this.goodsId = goodsId;
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

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
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

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public boolean isDefect()
		{
			return defect;
		}

		public void setDefect(boolean defect)
		{
			this.defect = defect;
		}

		public Integer getWarehouseId()
		{
			return warehouseId;
		}

		public void setWarehouseId(Integer warehouseId)
		{
			this.warehouseId = warehouseId;
		}

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
		}

		public BigDecimal getBeginCost()
		{
			return beginCost;
		}

		public void setBeginCost(BigDecimal beginCost)
		{
			this.beginCost = beginCost;
		}

		public BigDecimal getEndCost()
		{
			return endCost;
		}

		public void setEndCost(BigDecimal endCost)
		{
			this.endCost = endCost;
		}

		public BigDecimal getBeginStock()
		{
			return beginStock;
		}

		public void setBeginStock(BigDecimal beginStock)
		{
			this.beginStock = beginStock;
		}

		public BigDecimal getBeginCosttotal()
		{
			return beginCosttotal;
		}

		public void setBeginCosttotal(BigDecimal beginCosttotal)
		{
			this.beginCosttotal = beginCosttotal;
		}

		public BigDecimal getEndStock()
		{
			return endStock;
		}

		public void setEndStock(BigDecimal endStock)
		{
			this.endStock = endStock;
		}

		public BigDecimal getEndCosttotal()
		{
			return endCosttotal;
		}

		public void setEndCosttotal(BigDecimal endCosttotal)
		{
			this.endCosttotal = endCosttotal;
		}

		public BigDecimal getStockinSettleAmount()
		{
			return stockinSettleAmount;
		}

		public void setStockinSettleAmount(BigDecimal stockinSettleAmount)
		{
			this.stockinSettleAmount = stockinSettleAmount;
		}

		public BigDecimal getAverageStock()
		{
			return averageStock;
		}

		public void setAverageStock(BigDecimal averageStock)
		{
			this.averageStock = averageStock;
		}

		public BigDecimal getStockTurnover()
		{
			return stockTurnover;
		}

		public void setStockTurnover(BigDecimal stockTurnover)
		{
			this.stockTurnover = stockTurnover;
		}

		public BigDecimal getStockoutCount()
		{
			return stockoutCount;
		}

		public void setStockoutCount(BigDecimal stockoutCount)
		{
			this.stockoutCount = stockoutCount;
		}

		public BigDecimal getStockoutCost()
		{
			return stockoutCost;
		}

		public void setStockoutCost(BigDecimal stockoutCost)
		{
			this.stockoutCost = stockoutCost;
		}

		public BigDecimal getStockinCount()
		{
			return stockinCount;
		}

		public void setStockinCount(BigDecimal stockinCount)
		{
			this.stockinCount = stockinCount;
		}

		public BigDecimal getStockinMoney()
		{
			return stockinMoney;
		}

		public void setStockinMoney(BigDecimal stockinMoney)
		{
			this.stockinMoney = stockinMoney;
		}

		public BigDecimal getSellCount()
		{
			return sellCount;
		}

		public void setSellCount(BigDecimal sellCount)
		{
			this.sellCount = sellCount;
		}

		public BigDecimal getSellCost()
		{
			return sellCost;
		}

		public void setSellCost(BigDecimal sellCost)
		{
			this.sellCost = sellCost;
		}

		public BigDecimal getSellMoney()
		{
			return sellMoney;
		}

		public void setSellMoney(BigDecimal sellMoney)
		{
			this.sellMoney = sellMoney;
		}

		public BigDecimal getTraOutCount()
		{
			return traOutCount;
		}

		public void setTraOutCount(BigDecimal traOutCount)
		{
			this.traOutCount = traOutCount;
		}

		public BigDecimal getTraOutCost()
		{
			return traOutCost;
		}

		public void setTraOutCost(BigDecimal traOutCost)
		{
			this.traOutCost = traOutCost;
		}

		public BigDecimal getTraOutMoney()
		{
			return traOutMoney;
		}

		public void setTraOutMoney(BigDecimal traOutMoney)
		{
			this.traOutMoney = traOutMoney;
		}

		public BigDecimal getPurBackCount()
		{
			return purBackCount;
		}

		public void setPurBackCount(BigDecimal purBackCount)
		{
			this.purBackCount = purBackCount;
		}

		public BigDecimal getPurBackCost()
		{
			return purBackCost;
		}

		public void setPurBackCost(BigDecimal purBackCost)
		{
			this.purBackCost = purBackCost;
		}

		public BigDecimal getPurBackMoney()
		{
			return purBackMoney;
		}

		public void setPurBackMoney(BigDecimal purBackMoney)
		{
			this.purBackMoney = purBackMoney;
		}

		public BigDecimal getPdOutCount()
		{
			return pdOutCount;
		}

		public void setPdOutCount(BigDecimal pdOutCount)
		{
			this.pdOutCount = pdOutCount;
		}

		public BigDecimal getPdOutCost()
		{
			return pdOutCost;
		}

		public void setPdOutCost(BigDecimal pdOutCost)
		{
			this.pdOutCost = pdOutCost;
		}

		public BigDecimal getPdOutMoney()
		{
			return pdOutMoney;
		}

		public void setPdOutMoney(BigDecimal pdOutMoney)
		{
			this.pdOutMoney = pdOutMoney;
		}

		public BigDecimal getOtherOutCount()
		{
			return otherOutCount;
		}

		public void setOtherOutCount(BigDecimal otherOutCount)
		{
			this.otherOutCount = otherOutCount;
		}

		public BigDecimal getOtherOutCost()
		{
			return otherOutCost;
		}

		public void setOtherOutCost(BigDecimal otherOutCost)
		{
			this.otherOutCost = otherOutCost;
		}

		public BigDecimal getOtherOutMoney()
		{
			return otherOutMoney;
		}

		public void setOtherOutMoney(BigDecimal otherOutMoney)
		{
			this.otherOutMoney = otherOutMoney;
		}

		public BigDecimal getIntOutCount()
		{
			return intOutCount;
		}

		public void setIntOutCount(BigDecimal intOutCount)
		{
			this.intOutCount = intOutCount;
		}

		public BigDecimal getIntOutCost()
		{
			return intOutCost;
		}

		public void setIntOutCost(BigDecimal intOutCost)
		{
			this.intOutCost = intOutCost;
		}

		public BigDecimal getIntOutMoney()
		{
			return intOutMoney;
		}

		public void setIntOutMoney(BigDecimal intOutMoney)
		{
			this.intOutMoney = intOutMoney;
		}

		public BigDecimal getProOutCount()
		{
			return proOutCount;
		}

		public void setProOutCount(BigDecimal proOutCount)
		{
			this.proOutCount = proOutCount;
		}

		public BigDecimal getProOutCost()
		{
			return proOutCost;
		}

		public void setProOutCost(BigDecimal proOutCost)
		{
			this.proOutCost = proOutCost;
		}

		public BigDecimal getProOutMoney()
		{
			return proOutMoney;
		}

		public void setProOutMoney(BigDecimal proOutMoney)
		{
			this.proOutMoney = proOutMoney;
		}

		public BigDecimal getOuterOutCount()
		{
			return outerOutCount;
		}

		public void setOuterOutCount(BigDecimal outerOutCount)
		{
			this.outerOutCount = outerOutCount;
		}

		public BigDecimal getOuterOutCost()
		{
			return outerOutCost;
		}

		public void setOuterOutCost(BigDecimal outerOutCost)
		{
			this.outerOutCost = outerOutCost;
		}

		public BigDecimal getOuterOutMoney()
		{
			return outerOutMoney;
		}

		public void setOuterOutMoney(BigDecimal outerOutMoney)
		{
			this.outerOutMoney = outerOutMoney;
		}

		public BigDecimal getDefectOutCount()
		{
			return defectOutCount;
		}

		public void setDefectOutCount(BigDecimal defectOutCount)
		{
			this.defectOutCount = defectOutCount;
		}

		public BigDecimal getDefectOutCost()
		{
			return defectOutCost;
		}

		public void setDefectOutCost(BigDecimal defectOutCost)
		{
			this.defectOutCost = defectOutCost;
		}

		public BigDecimal getDefectOutMoney()
		{
			return defectOutMoney;
		}

		public void setDefectOutMoney(BigDecimal defectOutMoney)
		{
			this.defectOutMoney = defectOutMoney;
		}

		public BigDecimal getPurInCount()
		{
			return purInCount;
		}

		public void setPurInCount(BigDecimal purInCount)
		{
			this.purInCount = purInCount;
		}

		public BigDecimal getPurInMoney()
		{
			return purInMoney;
		}

		public void setPurInMoney(BigDecimal purInMoney)
		{
			this.purInMoney = purInMoney;
		}

		public BigDecimal getTraInCount()
		{
			return traInCount;
		}

		public void setTraInCount(BigDecimal traInCount)
		{
			this.traInCount = traInCount;
		}

		public BigDecimal getTraInMoney()
		{
			return traInMoney;
		}

		public void setTraInMoney(BigDecimal traInMoney)
		{
			this.traInMoney = traInMoney;
		}

		public BigDecimal getSellbackCount()
		{
			return sellbackCount;
		}

		public void setSellbackCount(BigDecimal sellbackCount)
		{
			this.sellbackCount = sellbackCount;
		}

		public BigDecimal getSellbackMoney()
		{
			return sellbackMoney;
		}

		public void setSellbackMoney(BigDecimal sellbackMoney)
		{
			this.sellbackMoney = sellbackMoney;
		}

		public BigDecimal getPdInCount()
		{
			return pdInCount;
		}

		public void setPdInCount(BigDecimal pdInCount)
		{
			this.pdInCount = pdInCount;
		}

		public BigDecimal getPdInMoney()
		{
			return pdInMoney;
		}

		public void setPdInMoney(BigDecimal pdInMoney)
		{
			this.pdInMoney = pdInMoney;
		}

		public BigDecimal getOtherInCount()
		{
			return otherInCount;
		}

		public void setOtherInCount(BigDecimal otherInCount)
		{
			this.otherInCount = otherInCount;
		}

		public BigDecimal getOtherInMoney()
		{
			return otherInMoney;
		}

		public void setOtherInMoney(BigDecimal otherInMoney)
		{
			this.otherInMoney = otherInMoney;
		}

		public BigDecimal getIntInCount()
		{
			return intInCount;
		}

		public void setIntInCount(BigDecimal intInCount)
		{
			this.intInCount = intInCount;
		}

		public BigDecimal getIntInMoney()
		{
			return intInMoney;
		}

		public void setIntInMoney(BigDecimal intInMoney)
		{
			this.intInMoney = intInMoney;
		}

		public BigDecimal getPreInCount()
		{
			return preInCount;
		}

		public void setPreInCount(BigDecimal preInCount)
		{
			this.preInCount = preInCount;
		}

		public BigDecimal getPreInMoney()
		{
			return preInMoney;
		}

		public void setPreInMoney(BigDecimal preInMoney)
		{
			this.preInMoney = preInMoney;
		}

		public BigDecimal getProInCount()
		{
			return proInCount;
		}

		public void setProInCount(BigDecimal proInCount)
		{
			this.proInCount = proInCount;
		}

		public BigDecimal getProInMoney()
		{
			return proInMoney;
		}

		public void setProInMoney(BigDecimal proInMoney)
		{
			this.proInMoney = proInMoney;
		}

		public BigDecimal getProductInCount()
		{
			return productInCount;
		}

		public void setProductInCount(BigDecimal productInCount)
		{
			this.productInCount = productInCount;
		}

		public BigDecimal getProductInMoney()
		{
			return productInMoney;
		}

		public void setProductInMoney(BigDecimal productInMoney)
		{
			this.productInMoney = productInMoney;
		}

		public BigDecimal getOuterInCount()
		{
			return outerInCount;
		}

		public void setOuterInCount(BigDecimal outerInCount)
		{
			this.outerInCount = outerInCount;
		}

		public BigDecimal getOuterInMoney()
		{
			return outerInMoney;
		}

		public void setOuterInMoney(BigDecimal outerInMoney)
		{
			this.outerInMoney = outerInMoney;
		}

		public BigDecimal getDefectInCount()
		{
			return defectInCount;
		}

		public void setDefectInCount(BigDecimal defectInCount)
		{
			this.defectInCount = defectInCount;
		}

		public BigDecimal getDefectInMoney()
		{
			return defectInMoney;
		}

		public void setDefectInMoney(BigDecimal defectInMoney)
		{
			this.defectInMoney = defectInMoney;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getProp3()
		{
			return prop3;
		}

		public void setProp3(String prop3)
		{
			this.prop3 = prop3;
		}

		public String getProp4()
		{
			return prop4;
		}

		public void setProp4(String prop4)
		{
			this.prop4 = prop4;
		}

		public String getProp5()
		{
			return prop5;
		}

		public void setProp5(String prop5)
		{
			this.prop5 = prop5;
		}

		public String getProp6()
		{
			return prop6;
		}

		public void setProp6(String prop6)
		{
			this.prop6 = prop6;
		}

		public String getProviderName()
		{
			return providerName;
		}

		public void setProviderName(String providerName)
		{
			this.providerName = providerName;
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

	public List<Detail> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<Detail> detailList)
	{
		this.detailList = detailList;
	}
}
