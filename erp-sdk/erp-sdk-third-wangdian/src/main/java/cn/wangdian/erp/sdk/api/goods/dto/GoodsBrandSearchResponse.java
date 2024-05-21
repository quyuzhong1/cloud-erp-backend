package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoodsBrandSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("detail_list")
	private List<GoodsBrandDto> detailList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<GoodsBrandDto> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<GoodsBrandDto> detailList)
	{
		this.detailList = detailList;
	}

	public static class GoodsBrandDto
	{
		@SerializedName("brand_id")
		private Integer brandId;
		@SerializedName("brand_no")
		private String brandNo;
		@SerializedName("brand_name")
		private String brandName;
		@SerializedName("is_disabled")
		private Boolean disabled;
		@SerializedName("remark")
		private String remark;
		@SerializedName("created")
		private String created;
		@SerializedName("modified")
		private String modified;

		public Integer getBrandId()
		{
			return brandId;
		}

		public void setBrandId(Integer brandId)
		{
			this.brandId = brandId;
		}

		public String getBrandNo()
		{
			return brandNo;
		}

		public void setBrandNo(String brandNo)
		{
			this.brandNo = brandNo;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public Boolean getDisabled()
		{
			return disabled;
		}

		public void setDisabled(Boolean disabled)
		{
			this.disabled = disabled;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}
	}
}
