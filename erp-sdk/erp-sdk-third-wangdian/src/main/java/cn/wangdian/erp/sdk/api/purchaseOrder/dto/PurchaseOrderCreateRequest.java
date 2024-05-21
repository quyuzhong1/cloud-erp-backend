package cn.wangdian.erp.sdk.api.purchaseOrder.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseOrderCreateRequest
{
	@SerializedName("purchase_no")
	private String purchaseNo;

	@SerializedName("provider_no")
	private String providerNo;

	@SerializedName("receive_warehouse_nos")
	private String receiveWarehouseNos;

	@SerializedName("expect_warehouse_no")
	private String expectWarehouseNos;

	@SerializedName("purchaser_name")
	private String purchaserName;

	@SerializedName("is_check")
	private Boolean check;

	@SerializedName("apply_nos")
	private String applyNos;

	@SerializedName("receive_address")
	private String receiveAddress;

	@SerializedName("contact")
	private String contact;

	@SerializedName("telno")
	private String telno;

	@SerializedName("logistics_name")
	private String logisticsName;

	@SerializedName("flag_name")
	private String flagName;

	@SerializedName("expect_time")
	private String expectTime;

	@SerializedName("created")
	private String created;

	@SerializedName("pay_type")
	private Byte payType;

	@SerializedName("postfee_pay_type")
	private Byte postfeePayType;

	@SerializedName("remark")
	private String remark;

	@SerializedName("post_fee")
	private BigDecimal postFee;

	@SerializedName("other_fee")
	private BigDecimal otherFee;

	@SerializedName("purchase_details")
	private List<PurchaseDetail> detailList;

	public static class PurchaseDetail
	{
		@SerializedName("spec_no")
		private String specNo;

		@SerializedName("num")
		private BigDecimal num;

		@SerializedName("purchase_unit_name")
		private String purchaseUnitName;

		@SerializedName("prop1")
		private String prop1;

		@SerializedName("prop2")
		private String prop2;

		@SerializedName("price")
		private BigDecimal price;

		@SerializedName("discount")
		private BigDecimal discount;

		@SerializedName("tax_rate")
		private BigDecimal taxRate;

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public String getPurchaseUnitName()
		{
			return purchaseUnitName;
		}

		public void setPurchaseUnitName(String purchaseUnitName)
		{
			this.purchaseUnitName = purchaseUnitName;
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

		public BigDecimal getPrice()
		{
			return price;
		}

		public void setPrice(BigDecimal price)
		{
			this.price = price;
		}

		public BigDecimal getDiscount()
		{
			return discount;
		}

		public void setDiscount(BigDecimal discount)
		{
			this.discount = discount;
		}

		public BigDecimal getTaxRate()
		{
			return taxRate;
		}

		public void setTaxRate(BigDecimal taxRate)
		{
			this.taxRate = taxRate;
		}
	}

	public String getPurchaseNo()
	{
		return purchaseNo;
	}

	public void setPurchaseNo(String purchaseNo)
	{
		this.purchaseNo = purchaseNo;
	}

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getReceiveWarehouseNos()
	{
		return receiveWarehouseNos;
	}

	public void setReceiveWarehouseNos(String receiveWarehouseNos)
	{
		this.receiveWarehouseNos = receiveWarehouseNos;
	}

	public String getExpectWarehouseNos()
	{
		return expectWarehouseNos;
	}

	public void setExpectWarehouseNos(String expectWarehouseNos)
	{
		this.expectWarehouseNos = expectWarehouseNos;
	}

	public String getPurchaserName()
	{
		return purchaserName;
	}

	public void setPurchaserName(String purchaserName)
	{
		this.purchaserName = purchaserName;
	}

	public Boolean getCheck()
	{
		return check;
	}

	public void setCheck(Boolean check)
	{
		this.check = check;
	}

	public String getApplyNos()
	{
		return applyNos;
	}

	public void setApplyNos(String applyNos)
	{
		this.applyNos = applyNos;
	}

	public String getReceiveAddress()
	{
		return receiveAddress;
	}

	public void setReceiveAddress(String receiveAddress)
	{
		this.receiveAddress = receiveAddress;
	}

	public String getContact()
	{
		return contact;
	}

	public void setContact(String contact)
	{
		this.contact = contact;
	}

	public String getTelno()
	{
		return telno;
	}

	public void setTelno(String telno)
	{
		this.telno = telno;
	}

	public String getLogisticsName()
	{
		return logisticsName;
	}

	public void setLogisticsName(String logisticsName)
	{
		this.logisticsName = logisticsName;
	}

	public String getFlagName()
	{
		return flagName;
	}

	public void setFlagName(String flagName)
	{
		this.flagName = flagName;
	}

	public String getExpectTime()
	{
		return expectTime;
	}

	public void setExpectTime(String expectTime)
	{
		this.expectTime = expectTime;
	}

	public String getCreated()
	{
		return created;
	}

	public void setCreated(String created)
	{
		this.created = created;
	}

	public Byte getPayType()
	{
		return payType;
	}

	public void setPayType(Byte payType)
	{
		this.payType = payType;
	}

	public Byte getPostfeePayType()
	{
		return postfeePayType;
	}

	public void setPostfeePayType(Byte postfeePayType)
	{
		this.postfeePayType = postfeePayType;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public BigDecimal getPostFee()
	{
		return postFee;
	}

	public void setPostFee(BigDecimal postFee)
	{
		this.postFee = postFee;
	}

	public BigDecimal getOtherFee()
	{
		return otherFee;
	}

	public void setOtherFee(BigDecimal otherFee)
	{
		this.otherFee = otherFee;
	}

	public List<PurchaseDetail> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<PurchaseDetail> detailList)
	{
		this.detailList = detailList;
	}
}
