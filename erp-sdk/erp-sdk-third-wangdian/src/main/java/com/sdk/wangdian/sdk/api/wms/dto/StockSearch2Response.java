package com.sdk.wangdian.sdk.api.wms.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class StockSearch2Response
{
	@SerializedName("total_count") private Integer total;
	@SerializedName("detail_list") private List<Detail> detailList;

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

	public static class Detail
	{
		@SerializedName("spec_code") private String specCode;
		@SerializedName("to_transfer_num") private BigDecimal toTransferNum;
		@SerializedName("warehouse_type") private Integer warehouseType;
		@SerializedName("brand_no") private String brandNo;
		@SerializedName("wms_stock_diff") private Integer wmsStockDiff;
		@SerializedName("refund_exch_num") private BigDecimal refundExchNum;
		@SerializedName("spec_no") private String specNo;
		@SerializedName("defect") private Boolean defect;
		@SerializedName("modified") private Long modified;
		@SerializedName("barcode") private String barcode;
		@SerializedName("part_paid_num") private BigDecimal partPaidNum;
		@SerializedName("goods_name") private String goodsName;
		@SerializedName("lock_num") private BigDecimal lockNum;
		@SerializedName("created") private Long created;
		@SerializedName("available_send_stock") private BigDecimal availableSendStock;
		@SerializedName("subscribe_num") private BigDecimal subscribeNum;
		@SerializedName("weight") private BigDecimal weight;
		@SerializedName("brand_name") private String brandName;
		@SerializedName("unpay_num") private BigDecimal unpayNum;
		@SerializedName("sending_num") private BigDecimal sendingNum;
		@SerializedName("to_process_in_num") private BigDecimal toProcessInNum;
		@SerializedName("warehouse_name") private String warehouseName;
		@SerializedName("img_url") private String imgUrl;
		@SerializedName("refund_num") private BigDecimal refundNum;
		@SerializedName("to_process_out_num") private BigDecimal toProcessOutNum;
		@SerializedName("order_num") private BigDecimal orderNum;
		@SerializedName("status") private Integer status;
		@SerializedName("flag_id") private Integer flagId;
		@SerializedName("wms_sync_stock") private BigDecimal wmsSyncStock;
		@SerializedName("to_purchase_num") private BigDecimal toPurchaseNum;
		@SerializedName("wms_preempty_stock") private BigDecimal wmsPreemptyStock;
		@SerializedName("goods_no") private String goodsNo;
		@SerializedName("stock_num") private BigDecimal stockNum;
		@SerializedName("purchase_arrive_num") private BigDecimal purchaseArriveNum;
		@SerializedName("wms_preempty_diff") private BigDecimal wmsPreemptyDiff;
		@SerializedName("remark") private String remark;
		@SerializedName("flag_name") private String flagName;
		@SerializedName("return_num") private BigDecimal returnNum;
		@SerializedName("purchase_num") private BigDecimal purchaseNum;
		@SerializedName("warehouse_no") private String warehouseNo;
		@SerializedName("spec_id") private Integer specId;
		@SerializedName("return_exch_num") private BigDecimal returnExchNum;
		@SerializedName("rec_id") private Integer recId;
		@SerializedName("to_other_in_num") private BigDecimal toOtherInNum;
		@SerializedName("to_other_out_num") private BigDecimal toOtherOutNum;
		@SerializedName("refund_onway_num") private BigDecimal refundOnwayNum;
		@SerializedName("transfer_num") private BigDecimal transferNum;
		@SerializedName("spec_name") private String specName;
		@SerializedName("return_onway_num") private BigDecimal returnOnwayNum;
		@SerializedName("warehouse_id") private Integer warehouseId;
		@SerializedName("last_inout_time") private String lastInoutTime;
		@SerializedName("last_pd_time") private String lastPdTime;

		private String erpWarehouseId;

		public String getErpWarehouseId() {
			return erpWarehouseId;
		}

		public void setErpWarehouseId(String erpWarehouseId) {
			this.erpWarehouseId = erpWarehouseId;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public BigDecimal getToTransferNum()
		{
			return toTransferNum;
		}

		public void setToTransferNum(BigDecimal toTransferNum)
		{
			this.toTransferNum = toTransferNum;
		}

		public Integer getWarehouseType()
		{
			return warehouseType;
		}

		public void setWarehouseType(Integer warehouseType)
		{
			this.warehouseType = warehouseType;
		}

		public String getBrandNo()
		{
			return brandNo;
		}

		public void setBrandNo(String brandNo)
		{
			this.brandNo = brandNo;
		}

		public Integer getWmsStockDiff()
		{
			return wmsStockDiff;
		}

		public void setWmsStockDiff(Integer wmsStockDiff)
		{
			this.wmsStockDiff = wmsStockDiff;
		}

		public BigDecimal getRefundExchNum()
		{
			return refundExchNum;
		}

		public void setRefundExchNum(BigDecimal refundExchNum)
		{
			this.refundExchNum = refundExchNum;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public Boolean getDefect()
		{
			return defect;
		}

		public void setDefect(Boolean defect)
		{
			this.defect = defect;
		}

		public Long getModified()
		{
			return modified;
		}

		public void setModified(Long modified)
		{
			this.modified = modified;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public BigDecimal getPartPaidNum()
		{
			return partPaidNum;
		}

		public void setPartPaidNum(BigDecimal partPaidNum)
		{
			this.partPaidNum = partPaidNum;
		}

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public BigDecimal getLockNum()
		{
			return lockNum;
		}

		public void setLockNum(BigDecimal lockNum)
		{
			this.lockNum = lockNum;
		}

		public Long getCreated()
		{
			return created;
		}

		public void setCreated(Long created)
		{
			this.created = created;
		}

		public BigDecimal getAvailableSendStock()
		{
			return availableSendStock;
		}

		public void setAvailableSendStock(BigDecimal availableSendStock)
		{
			this.availableSendStock = availableSendStock;
		}

		public BigDecimal getSubscribeNum()
		{
			return subscribeNum;
		}

		public void setSubscribeNum(BigDecimal subscribeNum)
		{
			this.subscribeNum = subscribeNum;
		}

		public BigDecimal getWeight()
		{
			return weight;
		}

		public void setWeight(BigDecimal weight)
		{
			this.weight = weight;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public BigDecimal getUnpayNum()
		{
			return unpayNum;
		}

		public void setUnpayNum(BigDecimal unpayNum)
		{
			this.unpayNum = unpayNum;
		}

		public BigDecimal getSendingNum()
		{
			return sendingNum;
		}

		public void setSendingNum(BigDecimal sendingNum)
		{
			this.sendingNum = sendingNum;
		}

		public BigDecimal getToProcessInNum()
		{
			return toProcessInNum;
		}

		public void setToProcessInNum(BigDecimal toProcessInNum)
		{
			this.toProcessInNum = toProcessInNum;
		}

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
		}

		public String getImgUrl()
		{
			return imgUrl;
		}

		public void setImgUrl(String imgUrl)
		{
			this.imgUrl = imgUrl;
		}

		public BigDecimal getRefundNum()
		{
			return refundNum;
		}

		public void setRefundNum(BigDecimal refundNum)
		{
			this.refundNum = refundNum;
		}

		public BigDecimal getToProcessOutNum()
		{
			return toProcessOutNum;
		}

		public void setToProcessOutNum(BigDecimal toProcessOutNum)
		{
			this.toProcessOutNum = toProcessOutNum;
		}

		public BigDecimal getOrderNum()
		{
			return orderNum;
		}

		public void setOrderNum(BigDecimal orderNum)
		{
			this.orderNum = orderNum;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
		}

		public Integer getFlagId()
		{
			return flagId;
		}

		public void setFlagId(Integer flagId)
		{
			this.flagId = flagId;
		}

		public BigDecimal getWmsSyncStock()
		{
			return wmsSyncStock;
		}

		public void setWmsSyncStock(BigDecimal wmsSyncStock)
		{
			this.wmsSyncStock = wmsSyncStock;
		}

		public BigDecimal getToPurchaseNum()
		{
			return toPurchaseNum;
		}

		public void setToPurchaseNum(BigDecimal toPurchaseNum)
		{
			this.toPurchaseNum = toPurchaseNum;
		}

		public BigDecimal getWmsPreemptyStock()
		{
			return wmsPreemptyStock;
		}

		public void setWmsPreemptyStock(BigDecimal wmsPreemptyStock)
		{
			this.wmsPreemptyStock = wmsPreemptyStock;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public BigDecimal getStockNum()
		{
			return stockNum;
		}

		public void setStockNum(BigDecimal stockNum)
		{
			this.stockNum = stockNum;
		}

		public BigDecimal getPurchaseArriveNum()
		{
			return purchaseArriveNum;
		}

		public void setPurchaseArriveNum(BigDecimal purchaseArriveNum)
		{
			this.purchaseArriveNum = purchaseArriveNum;
		}

		public BigDecimal getWmsPreemptyDiff()
		{
			return wmsPreemptyDiff;
		}

		public void setWmsPreemptyDiff(BigDecimal wmsPreemptyDiff)
		{
			this.wmsPreemptyDiff = wmsPreemptyDiff;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getFlagName()
		{
			return flagName;
		}

		public void setFlagName(String flagName)
		{
			this.flagName = flagName;
		}

		public BigDecimal getReturnNum()
		{
			return returnNum;
		}

		public void setReturnNum(BigDecimal returnNum)
		{
			this.returnNum = returnNum;
		}

		public BigDecimal getPurchaseNum()
		{
			return purchaseNum;
		}

		public void setPurchaseNum(BigDecimal purchaseNum)
		{
			this.purchaseNum = purchaseNum;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public Integer getSpecId()
		{
			return specId;
		}

		public void setSpecId(Integer specId)
		{
			this.specId = specId;
		}

		public BigDecimal getReturnExchNum()
		{
			return returnExchNum;
		}

		public void setReturnExchNum(BigDecimal returnExchNum)
		{
			this.returnExchNum = returnExchNum;
		}

		public Integer getRecId()
		{
			return recId;
		}

		public void setRecId(Integer recId)
		{
			this.recId = recId;
		}

		public BigDecimal getToOtherInNum()
		{
			return toOtherInNum;
		}

		public void setToOtherInNum(BigDecimal toOtherInNum)
		{
			this.toOtherInNum = toOtherInNum;
		}

		public BigDecimal getToOtherOutNum()
		{
			return toOtherOutNum;
		}

		public void setToOtherOutNum(BigDecimal toOtherOutNum)
		{
			this.toOtherOutNum = toOtherOutNum;
		}

		public BigDecimal getRefundOnwayNum()
		{
			return refundOnwayNum;
		}

		public void setRefundOnwayNum(BigDecimal refundOnwayNum)
		{
			this.refundOnwayNum = refundOnwayNum;
		}

		public BigDecimal getTransferNum()
		{
			return transferNum;
		}

		public void setTransferNum(BigDecimal transferNum)
		{
			this.transferNum = transferNum;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public BigDecimal getReturnOnwayNum()
		{
			return returnOnwayNum;
		}

		public void setReturnOnwayNum(BigDecimal returnOnwayNum)
		{
			this.returnOnwayNum = returnOnwayNum;
		}

		public Integer getWarehouseId()
		{
			return warehouseId;
		}

		public void setWarehouseId(Integer warehouseId)
		{
			this.warehouseId = warehouseId;
		}

		public String getLastInoutTime()
		{
			return lastInoutTime;
		}

		public void setLastInoutTime(String lastInoutTime)
		{
			this.lastInoutTime = lastInoutTime;
		}

		public String getLastPdTime()
		{
			return lastPdTime;
		}

		public void setLastPdTime(String lastPdTime)
		{
			this.lastPdTime = lastPdTime;
		}
	}
}
