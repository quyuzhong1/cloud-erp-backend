package cn.wangdian.erp.sdk.api.wms.stockother.out.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class StockOtherOutQueryResponse
{

	@SerializedName("total_count")
	private Integer total;
	@SerializedName("order")
	private List<Order> orderList;

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

	public static class Order
	{
		@SerializedName("logistics_name") private String logisticsName;
		@SerializedName("reason") private String reason;
		@SerializedName("receiver_city") private String receiverCity;
		@SerializedName("detail_list") private List<Detail> detailList;
		@SerializedName("remark") private String remark;
		@SerializedName("is_reserved") private Boolean reserved;
		@SerializedName("warehouse_no") private String warehouseNo;
		@SerializedName("modified") private Long modified;
		@SerializedName("receiver_name") private String receiverName;
		@SerializedName("note_count") private Integer noteCount;
		@SerializedName("receiver_province") private String receiverProvince;
		@SerializedName("other_out_no") private String otherOutNo;
		@SerializedName("logistics_no") private String logisticsNo;
		@SerializedName("created") private Long created;
		@SerializedName("receiver_district") private String receiverDistrict;
		@SerializedName("employee_name") private String employeeName;
		@SerializedName("rec_id") private Integer recId;
		@SerializedName("prop6") private String prop6;
		@SerializedName("prop5") private String prop5;
		@SerializedName("receiver_mobile") private String receiverMobile;
		@SerializedName("prop4") private String prop4;
		@SerializedName("prop3") private String prop3;
		@SerializedName("prop2") private String prop2;
		@SerializedName("prop1") private String prop1;
		@SerializedName("warehouse_name") private String warehouseName;
		@SerializedName("receiver_address") private String receiverAddress;
		@SerializedName("status") private Integer status;

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}

		public String getReceiverCity()
		{
			return receiverCity;
		}

		public void setReceiverCity(String receiverCity)
		{
			this.receiverCity = receiverCity;
		}

		public List<Detail> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<Detail> detailList)
		{
			this.detailList = detailList;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Boolean getReserved()
		{
			return reserved;
		}

		public void setReserved(Boolean reserved)
		{
			this.reserved = reserved;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public Long getModified()
		{
			return modified;
		}

		public void setModified(Long modified)
		{
			this.modified = modified;
		}

		public String getReceiverName()
		{
			return receiverName;
		}

		public void setReceiverName(String receiverName)
		{
			this.receiverName = receiverName;
		}

		public Integer getNoteCount()
		{
			return noteCount;
		}

		public void setNoteCount(Integer noteCount)
		{
			this.noteCount = noteCount;
		}

		public String getReceiverProvince()
		{
			return receiverProvince;
		}

		public void setReceiverProvince(String receiverProvince)
		{
			this.receiverProvince = receiverProvince;
		}

		public String getOtherOutNo()
		{
			return otherOutNo;
		}

		public void setOtherOutNo(String otherOutNo)
		{
			this.otherOutNo = otherOutNo;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public Long getCreated()
		{
			return created;
		}

		public void setCreated(Long created)
		{
			this.created = created;
		}

		public String getReceiverDistrict()
		{
			return receiverDistrict;
		}

		public void setReceiverDistrict(String receiverDistrict)
		{
			this.receiverDistrict = receiverDistrict;
		}

		public String getEmployeeName()
		{
			return employeeName;
		}

		public void setEmployeeName(String employeeName)
		{
			this.employeeName = employeeName;
		}

		public Integer getRecId()
		{
			return recId;
		}

		public void setRecId(Integer recId)
		{
			this.recId = recId;
		}

		public String getProp6()
		{
			return prop6;
		}

		public void setProp6(String prop6)
		{
			this.prop6 = prop6;
		}

		public String getProp5()
		{
			return prop5;
		}

		public void setProp5(String prop5)
		{
			this.prop5 = prop5;
		}

		public String getReceiverMobile()
		{
			return receiverMobile;
		}

		public void setReceiverMobile(String receiverMobile)
		{
			this.receiverMobile = receiverMobile;
		}

		public String getProp4()
		{
			return prop4;
		}

		public void setProp4(String prop4)
		{
			this.prop4 = prop4;
		}

		public String getProp3()
		{
			return prop3;
		}

		public void setProp3(String prop3)
		{
			this.prop3 = prop3;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
		}

		public String getReceiverAddress()
		{
			return receiverAddress;
		}

		public void setReceiverAddress(String receiverAddress)
		{
			this.receiverAddress = receiverAddress;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
		}

		public static class Detail
		{
			@SerializedName("rec_id") private Integer recId;
			@SerializedName("other_out_id") private Integer otherOutId;
			@SerializedName("num") private BigDecimal num;
			@SerializedName("num2") private BigDecimal num2;
			@SerializedName("defect") private Boolean defect;
			@SerializedName("expire_date") private String expireDate;
			@SerializedName("remark") private String remark;
			@SerializedName("goods_id") private Integer goodsId;
			@SerializedName("goods_name") private String goodsName;
			@SerializedName("short_name") private String shortName;
			@SerializedName("goods_no") private String goodsNo;
			@SerializedName("spec_code") private String specCode;
			@SerializedName("spec_name") private String specName;
			@SerializedName("spec_no") private String specNo;
			@SerializedName("barcode") private String barcode;
			@SerializedName("out_num") private BigDecimal outNum;

			public Integer getRecId()
			{
				return recId;
			}

			public void setRecId(Integer recId)
			{
				this.recId = recId;
			}

			public Integer getOtherOutId()
			{
				return otherOutId;
			}

			public void setOtherOutId(Integer otherOutId)
			{
				this.otherOutId = otherOutId;
			}

			public Boolean getDefect()
			{
				return defect;
			}

			public void setDefect(Boolean defect)
			{
				this.defect = defect;
			}

			public String getExpireDate()
			{
				return expireDate;
			}

			public void setExpireDate(String expireDate)
			{
				this.expireDate = expireDate;
			}

			public String getRemark()
			{
				return remark;
			}

			public void setRemark(String remark)
			{
				this.remark = remark;
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

			public String getShortName()
			{
				return shortName;
			}

			public void setShortName(String shortName)
			{
				this.shortName = shortName;
			}

			public String getGoodsNo()
			{
				return goodsNo;
			}

			public void setGoodsNo(String goodsNo)
			{
				this.goodsNo = goodsNo;
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

			public String getSpecNo()
			{
				return specNo;
			}

			public void setSpecNo(String specNo)
			{
				this.specNo = specNo;
			}

			public String getBarcode()
			{
				return barcode;
			}

			public void setBarcode(String barcode)
			{
				this.barcode = barcode;
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

			public BigDecimal getOutNum()
			{
				return outNum;
			}

			public void setOutNum(BigDecimal outNum)
			{
				this.outNum = outNum;
			}
		}
	}
}
