package cn.wangdian.erp.sdk.api.wms.stockother.in.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class StockOtherInQueryResponse
{
	@SerializedName("total_count") private Integer total;
	@SerializedName("order") private List<Order> orderList;

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
		@SerializedName("logistics_no") private String logisticsNo;
		@SerializedName("created") private Long created;
		@SerializedName("detail_list") private List<Detail> detailList;
		@SerializedName("remark") private String remark;
		@SerializedName("employee_name") private String employeeName;
		@SerializedName("rec_id") private Integer recId;
		@SerializedName("warehouse_name") private String warehouseName;
		@SerializedName("warehouse_no") private String warehouseNo;
		@SerializedName("modified") private Long modified;
		@SerializedName("other_in_no") private String otherInNo;
		@SerializedName("note_count") private Integer noteCount;
		@SerializedName("status") private Byte status;

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

		public List<Detail> getDetailList()
		{
			return detailList;
		}

		public void setDetailList(List<Detail> detail)
		{
			this.detailList = detail;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
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

		public String getWarehouseName()
		{
			return warehouseName;
		}

		public void setWarehouseName(String warehouseName)
		{
			this.warehouseName = warehouseName;
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

		public String getOtherInNo()
		{
			return otherInNo;
		}

		public void setOtherInNo(String otherInNo)
		{
			this.otherInNo = otherInNo;
		}

		public Integer getNoteCount()
		{
			return noteCount;
		}

		public void setNoteCount(Integer noteCount)
		{
			this.noteCount = noteCount;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}

		public static class Detail
		{
			@SerializedName("rec_id") private Integer recId;
			@SerializedName("other_in_id") private Integer otherInId;
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
			@SerializedName("in_num") private BigDecimal inNum;

			public Integer getRecId()
			{
				return recId;
			}

			public void setRecId(Integer recId)
			{
				this.recId = recId;
			}

			public Integer getOtherInId()
			{
				return otherInId;
			}

			public void setOtherInId(Integer otherInId)
			{
				this.otherInId = otherInId;
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

			public void setInNum(BigDecimal inNum)
			{
				this.inNum = inNum;
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
		}
	}
}
