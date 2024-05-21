package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LogisticsSyncGetResponse
{
	/**
	 * response:
	 * {"status":0,"total":1,"data":[{"sync_id":189,"tid":"gJTVV6yNdz","logistics_no":"99999999999999","logistics_code":"10001","logistics_name":"圆通速递","is_part_sync":1,"oids":"!!!!:2019-11-08
	 * 17:51:56","trade_id":8841,"platform_id":127}]}
	 */

	@SerializedName("total")
	private Integer totalCount;
	@SerializedName("data")
	private List<LogisticsSyncGetDto> logisticsSyncGetDtos;

	public Integer getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(Integer totalCount)
	{
		this.totalCount = totalCount;
	}

	public List<LogisticsSyncGetDto> getLogisticsSyncGetDtos()
	{
		return logisticsSyncGetDtos;
	}

	public void setLogisticsSyncGetDtos(List<LogisticsSyncGetDto> logisticsSyncGetDtos)
	{
		this.logisticsSyncGetDtos = logisticsSyncGetDtos;
	}

	public static class LogisticsSyncGetDto
	{
		@SerializedName("sync_id")
		private Integer syncId;
		@SerializedName("tid")
		private String tid;
		@SerializedName("logistics_no")
		private String logisticsNo;
		@SerializedName("logistics_code")
		private String logisticsCode;
		@SerializedName("logistics_name")
		private String logisticsName;
		@SerializedName("oids")
		private String oids;
		@SerializedName("is_part_sync")
		private Boolean partSync;
		@SerializedName("trade_id")
		private Integer tradeId;
		@SerializedName("platform_id")
		private Byte platformId;

		public Integer getSyncId()
		{
			return syncId;
		}

		public void setSyncId(Integer syncId)
		{
			this.syncId = syncId;
		}

		public String getTid()
		{
			return tid;
		}

		public void setTid(String tid)
		{
			this.tid = tid;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public String getLogisticsCode()
		{
			return logisticsCode;
		}

		public void setLogisticsCode(String logisticsCode)
		{
			this.logisticsCode = logisticsCode;
		}

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getOids()
		{
			return oids;
		}

		public void setOids(String oids)
		{
			this.oids = oids;
		}

		public Boolean getPartSync()
		{
			return partSync;
		}

		public void setPartSync(Boolean partSync)
		{
			this.partSync = partSync;
		}

		public Integer getTradeId()
		{
			return tradeId;
		}

		public void setTradeId(Integer tradeId)
		{
			this.tradeId = tradeId;
		}

		public Byte getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Byte platformId)
		{
			this.platformId = platformId;
		}
	}
}
