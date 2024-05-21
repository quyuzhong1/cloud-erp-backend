package cn.wangdian.erp.sdk.api.purchase.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PurchaseProviderGoodsUploadRequest
{
	@SerializedName("provider_no")
	private String providerNo;

	@SerializedName("spec_no")
	private String specNo;

	@SerializedName("provider_goods_no")
	private String providerGoodsNo;

	@SerializedName("lowest_price")
	private BigDecimal lowestPrice;

	@SerializedName("purchase_unit_name")
	private String purchaseUnitName;

	@SerializedName("min_purchase_num")
	private BigDecimal minPurchaseNum;

	@SerializedName("remark")
	private String remark;

	@SerializedName("tax_rate")
	private BigDecimal taxRate;

	@SerializedName("discount")
	private BigDecimal discount;

	@SerializedName("purchase_cycle_day")
	private Integer purchaseCycleDay;

	@SerializedName("is_disabled")
	private Boolean disabled;

	@SerializedName("is_master")
	private Boolean master;

	@SerializedName("price")
	private BigDecimal price;

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getSpecNo()
	{
		return specNo;
	}

	public void setSpecNo(String specNo)
	{
		this.specNo = specNo;
	}

	public String getProviderGoodsNo()
	{
		return providerGoodsNo;
	}

	public void setProviderGoodsNo(String providerGoodsNo)
	{
		this.providerGoodsNo = providerGoodsNo;
	}

	public BigDecimal getLowestPrice()
	{
		return lowestPrice;
	}

	public void setLowestPrice(BigDecimal lowestPrice)
	{
		this.lowestPrice = lowestPrice;
	}

	public String getPurchaseUnitName()
	{
		return purchaseUnitName;
	}

	public void setPurchaseUnitName(String purchaseUnitName)
	{
		this.purchaseUnitName = purchaseUnitName;
	}

	public BigDecimal getMinPurchaseNum()
	{
		return minPurchaseNum;
	}

	public void setMinPurchaseNum(BigDecimal minPurchaseNum)
	{
		this.minPurchaseNum = minPurchaseNum;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public BigDecimal getTaxRate()
	{
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate)
	{
		this.taxRate = taxRate;
	}

	public BigDecimal getDiscount()
	{
		return discount;
	}

	public void setDiscount(BigDecimal discount)
	{
		this.discount = discount;
	}

	public Integer getPurchaseCycleDay()
	{
		return purchaseCycleDay;
	}

	public void setPurchaseCycleDay(Integer purchaseCycleDay)
	{
		this.purchaseCycleDay = purchaseCycleDay;
	}

	public Boolean getDisabled()
	{
		return disabled;
	}

	public void setDisabled(Boolean disabled)
	{
		this.disabled = disabled;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public void setPrice(BigDecimal price)
	{
		this.price = price;
	}
}
