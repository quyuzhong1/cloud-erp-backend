package cn.wangdian.erp.sdk.api.purchasereturn.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PurchaseReturnCreateOrderRequest
{
	public static class PurchaseReturnOrderInfo
	{
		private String outerNo;
		private String warehouseNo;
		private String providerNo;
		private BigDecimal postFee;
		private BigDecimal otherFee;
		private String remark;
		@SerializedName("receive_province")
		private Integer province;
		@SerializedName("receive_city")
		private Integer city;
		@SerializedName("receive_district")
		private Integer district;
		@SerializedName("receive_address")
		private String address;
		public String getOuterNo() {
			return outerNo;
		}
		public void setOuterNo(String outerNo) {
			this.outerNo = outerNo;
		}
		public String getWarehouseNo() {
			return warehouseNo;
		}
		public void setWarehouseNo(String warehouseNo) {
			this.warehouseNo = warehouseNo;
		}
		public String getProviderNo() {
			return providerNo;
		}
		public void setProviderNo(String providerNo) {
			this.providerNo = providerNo;
		}
		public BigDecimal getPostFee() {
			return postFee;
		}
		public void setPostFee(BigDecimal postFee) {
			this.postFee = postFee;
		}
		public BigDecimal getOtherFee() {
			return otherFee;
		}
		public void setOtherFee(BigDecimal otherFee) {
			this.otherFee = otherFee;
		}
		public String getRemark() {
			return remark;
		}
		public void setRemark(String remark) {
			this.remark = remark;
		}

		public Integer getProvince()
		{
			return province;
		}

		public void setProvince(Integer province)
		{
			this.province = province;
		}

		public Integer getCity()
		{
			return city;
		}

		public void setCity(Integer city)
		{
			this.city = city;
		}

		public Integer getDistrict()
		{
			return district;
		}

		public void setDistrict(Integer district)
		{
			this.district = district;
		}
	}

	public static class PurchaseReturnDetail
	{
		private String specNo;
		private BigDecimal num;
		private BigDecimal discount;
		private BigDecimal price;
		private String remark;

		public String getSpecNo() {
			return specNo;
		}
		public void setSpecNo(String specNo) {
			this.specNo = specNo;
		}
		public BigDecimal getNum() {
			return num;
		}
		public void setNum(BigDecimal num) {
			this.num = num;
		}
		public BigDecimal getDiscount() {
			return discount;
		}
		public void setDiscount(BigDecimal discount) {
			this.discount = discount;
		}
		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
		public String getRemark() {
			return remark;
		}
		public void setRemark(String remark) {
			this.remark = remark;
		}

	}

}
