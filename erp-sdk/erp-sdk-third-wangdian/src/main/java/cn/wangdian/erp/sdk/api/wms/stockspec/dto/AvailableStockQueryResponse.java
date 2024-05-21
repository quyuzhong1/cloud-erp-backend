package cn.wangdian.erp.sdk.api.wms.stockspec.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class AvailableStockQueryResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("stocks")
	private List<AvailableStockQueryResponse.StockDto> stockDtoList;

	public static class StockDto
	{
		/**
		 * 0 未启用
		 */
		public static final byte STATUS_NO_USING = 0;
		/**
		 * 1 启用
		 */
		public static final byte STATUS_USING = 1;
		/**
		 * 2 停用
		 */
		public static final byte STATUS_DISABLE = 2;

		private String goodsName ;
		private String specCode ;
		private BigDecimal num ;
		private String warehouseNo ;
		private String goodsNo ;
		private String brandName ;
		private Byte type ;
		private String specNo ;
		private boolean defect ;
		private String shortName ;
		private String specName ;
		private String barcode ;
		private String className ;
		private Byte status ;

		public String getGoodsName()
		{
			return goodsName;
		}

		public void setGoodsName(String goodsName)
		{
			this.goodsName = goodsName;
		}

		public String getSpecCode()
		{
			return specCode;
		}

		public void setSpecCode(String specCode)
		{
			this.specCode = specCode;
		}

		public BigDecimal getNum()
		{
			return num;
		}

		public void setNum(BigDecimal num)
		{
			this.num = num;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public String getGoodsNo()
		{
			return goodsNo;
		}

		public void setGoodsNo(String goodsNo)
		{
			this.goodsNo = goodsNo;
		}

		public String getBrandName()
		{
			return brandName;
		}

		public void setBrandName(String brandName)
		{
			this.brandName = brandName;
		}

		public Byte getType()
		{
			return type;
		}

		public void setType(Byte type)
		{
			this.type = type;
		}

		public String getSpecNo()
		{
			return specNo;
		}

		public void setSpecNo(String specNo)
		{
			this.specNo = specNo;
		}

		public boolean isDefect()
		{
			return defect;
		}

		public void setDefect(boolean defect)
		{
			this.defect = defect;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String shortName)
		{
			this.shortName = shortName;
		}

		public String getSpecName()
		{
			return specName;
		}

		public void setSpecName(String specName)
		{
			this.specName = specName;
		}

		public String getBarcode()
		{
			return barcode;
		}

		public void setBarcode(String barcode)
		{
			this.barcode = barcode;
		}

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public Byte getStatus()
		{
			return status;
		}

		public void setStatus(Byte status)
		{
			this.status = status;
		}
	}

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<StockDto> getStockDtoList()
	{
		return stockDtoList;
	}

	public void setStockDtoList(List<StockDto> stockDtoList)
	{
		this.stockDtoList = stockDtoList;
	}
}
