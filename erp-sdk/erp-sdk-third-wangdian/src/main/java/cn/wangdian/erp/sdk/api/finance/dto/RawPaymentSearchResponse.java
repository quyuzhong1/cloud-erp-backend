package cn.wangdian.erp.sdk.api.finance.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class RawPaymentSearchResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("detail_list")
	private List<RawPaymentSearchResponse.detailInfoDto> detailList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<detailInfoDto> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<detailInfoDto> detailList)
	{
		this.detailList = detailList;
	}

	public static class detailInfoDto
	{
		private Integer platformId;
		private String shopNo;
		private String payOrderNo;
		private Integer status;
		private Integer type;
		private String orderNo;
		private BigDecimal inAmount;
		private BigDecimal outAmount;
		private BigDecimal num;
		private BigDecimal balance;
		private Boolean isGuarantee;
		private String remark;
		private String createTime;
		private String created;
		private String modified;

		public Integer getPlatformId()
		{
			return platformId;
		}

		public void setPlatfromId(Integer platformId)
		{
			this.platformId = platformId;
		}

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public Integer getStatus()
		{
			return status;
		}

		public void setStatus(Integer status)
		{
			this.status = status;
		}

		public Integer getType()
		{
			return type;
		}

		public void setType(Integer type)
		{
			this.type = type;
		}

		public String getOrderNo()
		{
			return orderNo;
		}

		public void setOrderNo(String orderNo)
		{
			this.orderNo = orderNo;
		}

		public String getPayOrderNo()
		{
			return payOrderNo;
		}

		public void setPayOrderNo(String payOrderNo)
		{
			this.payOrderNo = payOrderNo;
		}

		public BigDecimal getInAmount()
		{
			return inAmount;
		}

		public void setInAmount(BigDecimal inAmount)
		{
			this.inAmount = inAmount;
		}

		public BigDecimal getoutAmount()
		{
			return outAmount;
		}

		public void setOutAmount(BigDecimal outAmount)
		{
			this.outAmount = outAmount;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public BigDecimal getBalance()
		{
			return balance;
		}

		public void setBalance(BigDecimal balance)
		{
			this.balance = balance;
		}

		public Boolean getGuarantee()
		{
			return isGuarantee;
		}

		public void setGuarantee(Boolean guarantee)
		{
			isGuarantee = guarantee;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getCreateTime()
		{
			return createTime;
		}

		public void setCreateTime(String createTime)
		{
			this.createTime = createTime;
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

		@Override
		public String toString()
		{
			return "detailInfoDto{" + "platfromId=" + platformId + ", shopNo='" + shopNo + '\'' + ", status=" + status
					+ ", type=" + type + ", orderNo='" + orderNo + '\'' + ", payOrderNo='" + payOrderNo + '\''
					+ ", inAmount=" + inAmount + ", outAmount=" + outAmount + ", num=" + num + ", balance=" + balance
					+ ", isGuarantee=" + isGuarantee + ", remark='" + remark + '\'' + ", createTime='" + createTime
					+ '\'' + ", created='" + created + '\'' + ", modified='" + modified + '\'' + '}';
		}
	}
}
