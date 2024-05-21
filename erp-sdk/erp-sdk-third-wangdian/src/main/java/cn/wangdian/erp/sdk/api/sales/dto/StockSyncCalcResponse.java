package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class StockSyncCalcResponse
{
	@SerializedName("match_code")
	private String matchCode;

	@SerializedName("list_time")
	private String listTime;

	@SerializedName("stock_syn_min")
	private BigDecimal stockSynMin;

	@SerializedName("stock_num")
	private BigDecimal stockNum;

	@SerializedName("stock_syn_warehouses")
	private String stockSynWarehouses;

	@SerializedName("sub_platform_id")
	private Integer subPlatformId;

	@SerializedName("reserve_s")
	private String reserveS;

	@SerializedName("outer_id")
	private String outerId;

	@SerializedName("spec_outer_id")
	private String specOuterId;

	@SerializedName("stock_syn_percent")
	private BigDecimal stockSynPercent;

	@SerializedName("spec_id")
	private String specId;

	@SerializedName("is_auto_delisting")
	private Boolean isAutoDelisting;

	@SerializedName("stock_syn_plus")
	private BigDecimal stockSynPlus;

	@SerializedName("is_auto_listing")
	private Boolean isAutoListing;

	@SerializedName("match_target_id")
	private Integer matchTargetId;

	@SerializedName("match_target_type")
	private Byte matchTargetType;

	@SerializedName("mask")
	private Integer mask;

	@SerializedName("syn_stock")
	private BigDecimal synStock;

	@SerializedName("stock_syn_rule_id")
	private Integer stockSynRuleId;

	@SerializedName("stock_syn_mask")
	private Integer stockSynMask;

	@SerializedName("goods_id")
	private String goodsId;

	@SerializedName("rec_id")
	private Integer recId;

	@SerializedName("last_syn_num")
	private BigDecimal lastSynNum;

	@SerializedName("shop_id")
	private Integer shopId;

	@SerializedName("stock_syn_max")
	private BigDecimal stockSynMax;

	@SerializedName("last_syn_time")
	private String lastSynTime;

	@SerializedName("account_id")
	private String accountId;

	@SerializedName("app_key")
	private String appKey;

	@SerializedName("delist_time")
	private String delistTime;

	@SerializedName("platform_id")
	private Short platformId;

	@SerializedName("stock_syn_rule_no")
	private String stockSynRuleNo;

	@SerializedName("status")
	private Byte status;

	@SerializedName("stock_change_count")
	private Integer stockChangeCount;

	public String getMatchCode()
	{
		return matchCode;
	}

	public void setMatchCode(String matchCode)
	{
		this.matchCode = matchCode;
	}

	public String getListTime()
	{
		return listTime;
	}

	public void setListTime(String listTime)
	{
		this.listTime = listTime;
	}

	public BigDecimal getStockSynMin()
	{
		return stockSynMin;
	}

	public void setStockSynMin(BigDecimal stockSynMin)
	{
		this.stockSynMin = stockSynMin;
	}

	public BigDecimal getStockNum()
	{
		return stockNum;
	}

	public void setStockNum(BigDecimal stockNum)
	{
		this.stockNum = stockNum;
	}

	public String getStockSynWarehouses()
	{
		return stockSynWarehouses;
	}

	public void setStockSynWarehouses(String stockSynWarehouses)
	{
		this.stockSynWarehouses = stockSynWarehouses;
	}

	public Integer getSubPlatformId()
	{
		return subPlatformId;
	}

	public void setSubPlatformId(Integer subPlatformId)
	{
		this.subPlatformId = subPlatformId;
	}

	public String getReserveS()
	{
		return reserveS;
	}

	public void setReserveS(String reserveS)
	{
		this.reserveS = reserveS;
	}

	public String getOuterId()
	{
		return outerId;
	}

	public void setOuterId(String outerId)
	{
		this.outerId = outerId;
	}

	public String getSpecOuterId()
	{
		return specOuterId;
	}

	public void setSpecOuterId(String specOuterId)
	{
		this.specOuterId = specOuterId;
	}

	public BigDecimal getStockSynPercent()
	{
		return stockSynPercent;
	}

	public void setStockSynPercent(BigDecimal stockSynPercent)
	{
		this.stockSynPercent = stockSynPercent;
	}

	public String getSpecId()
	{
		return specId;
	}

	public void setSpecId(String specId)
	{
		this.specId = specId;
	}

	public Boolean getAutoDelisting()
	{
		return isAutoDelisting;
	}

	public void setAutoDelisting(Boolean autoDelisting)
	{
		isAutoDelisting = autoDelisting;
	}

	public BigDecimal getStockSynPlus()
	{
		return stockSynPlus;
	}

	public void setStockSynPlus(BigDecimal stockSynPlus)
	{
		this.stockSynPlus = stockSynPlus;
	}

	public Boolean getAutoListing()
	{
		return isAutoListing;
	}

	public void setAutoListing(Boolean autoListing)
	{
		isAutoListing = autoListing;
	}

	public Integer getMatchTargetId()
	{
		return matchTargetId;
	}

	public void setMatchTargetId(Integer matchTargetId)
	{
		this.matchTargetId = matchTargetId;
	}

	public Byte getMatchTargetType()
	{
		return matchTargetType;
	}

	public void setMatchTargetType(Byte matchTargetType)
	{
		this.matchTargetType = matchTargetType;
	}

	public Integer getMask()
	{
		return mask;
	}

	public void setMask(Integer mask)
	{
		this.mask = mask;
	}

	public BigDecimal getSynStock()
	{
		return synStock;
	}

	public void setSynStock(BigDecimal synStock)
	{
		this.synStock = synStock;
	}

	public Integer getStockSynRuleId()
	{
		return stockSynRuleId;
	}

	public void setStockSynRuleId(Integer stockSynRuleId)
	{
		this.stockSynRuleId = stockSynRuleId;
	}

	public Integer getStockSynMask()
	{
		return stockSynMask;
	}

	public void setStockSynMask(Integer stockSynMask)
	{
		this.stockSynMask = stockSynMask;
	}

	public String getGoodsId()
	{
		return goodsId;
	}

	public void setGoodsId(String goodsId)
	{
		this.goodsId = goodsId;
	}

	public Integer getRecId()
	{
		return recId;
	}

	public void setRecId(Integer recId)
	{
		this.recId = recId;
	}

	public BigDecimal getLastSynNum()
	{
		return lastSynNum;
	}

	public void setLastSynNum(BigDecimal lastSynNum)
	{
		this.lastSynNum = lastSynNum;
	}

	public Integer getShopId()
	{
		return shopId;
	}

	public void setShopId(Integer shopId)
	{
		this.shopId = shopId;
	}

	public BigDecimal getStockSynMax()
	{
		return stockSynMax;
	}

	public void setStockSynMax(BigDecimal stockSynMax)
	{
		this.stockSynMax = stockSynMax;
	}

	public String getLastSynTime()
	{
		return lastSynTime;
	}

	public void setLastSynTime(String lastSynTime)
	{
		this.lastSynTime = lastSynTime;
	}

	public String getAccountId()
	{
		return accountId;
	}

	public void setAccountId(String accountId)
	{
		this.accountId = accountId;
	}

	public String getAppKey()
	{
		return appKey;
	}

	public void setAppKey(String appKey)
	{
		this.appKey = appKey;
	}

	public String getDelistTime()
	{
		return delistTime;
	}

	public void setDelistTime(String delistTime)
	{
		this.delistTime = delistTime;
	}

	public Short getPlatformId()
	{
		return platformId;
	}

	public void setPlatformId(Short platformId)
	{
		this.platformId = platformId;
	}

	public String getStockSynRuleNo()
	{
		return stockSynRuleNo;
	}

	public void setStockSynRuleNo(String stockSynRuleNo)
	{
		this.stockSynRuleNo = stockSynRuleNo;
	}

	public Byte getStatus()
	{
		return status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
	}

	public Integer getStockChangeCount()
	{
		return stockChangeCount;
	}

	public void setStockChangeCount(Integer stockChangeCount)
	{
		this.stockChangeCount = stockChangeCount;
	}
}