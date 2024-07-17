package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class OtherStockinResponse {
	/*
{
    "status": 0,
    "data": {
        "total_count": 1,
        "order": [
            {
                "logistics_name": "无",
                "logistics_no": "",
                "created": 1592876966000,
                "detail_list": [
                    {
                        "goods_name": "sss的辣条",
                        "short_name": "",
                        "goods_no": "ES1011479",
                        "spec_code": "",
                        "spec_name": "sss的辣条:",
                        "spec_no": "BM20200519158",
                        "barcode": "",
                        "num": 4,
                        "num2": 4,
                        "position_no": "其它未上架",
                        "expect_num": 4,
                        "remark": "",
                        "weight": 0,
                        "goods_weight": 0,
                        "defect": false,
                        "unit_ratio": 1,
                        "validity_days": 0,
                        "need_inspect_num": 0,
                        "brand_name": "无",
                        "aux_unit_name": "无",
                        "base_unit_name": "无"
                    }
                ],
                "src_order_type": 6,
                "goods_type_count": 1,
                "remark": "",
                "goods_count": 4,
                "operator_name": "小二y",
                "warehouse_name": "巫妖的仓库",
                "producer_name": "【震惊！！】无人生产商",
                "stockin_no": "RK2006230002",
                "modified": 1592876982000,
                "process_no": "PS2020062202",
                "right_num": 4,
                "note_count": 0,
                "status": 80,
                "check_time": 1592876980000
            }
        ]
    }
}
	 */
	
	@SerializedName("status")
	@JSONField(name = "status")
	@JsonProperty("status")
	private Integer status;

	@SerializedName("message")
	@JSONField(name = "message")
	@JsonProperty("message")
	private String message;

	@SerializedName("data")
	@JSONField(name = "data")
	@JsonProperty( "data")
	private List<DataInfoDto> data;

	@Data
	public static class DataInfoDto
	{
		private List<OrderInfoDto> order;
		private Integer totalCount;
	}

	@Data
	public static class OrderInfoDto
	{

		private String stockinId;
		private String orderNo;
		private String warehouseNo;
		private Integer status;
		private String message;
		private String warehouseName;
		private String stockinTime;
		private String createdTime;
		private String reason;
		private String remark;
		private BigDecimal goodsCount;
		private Integer logisticsType;
		private String checkTime;
		private String srcOrderNo;
		private String operatorName;
		private BigDecimal totalPrice;

		@SerializedName("detail_list")
		private List<OrderDetailInfoDto> detailList;
	}

	@Data
	public static class OrderDetailInfoDto
	{
		private String stockinId;
		private BigDecimal goodsCount;
		private BigDecimal totalCost;
		private String remark;

		private BigDecimal rightNum;
		private String goodsUnit;
		private String batchNo;
		private Integer recId;
		private String goodsName;
		private String goodsNo;
		private String specNo;
		private String prop2;
		private String specName;
		private String specCode;
		private String brandNo;
		private String brandName;

	}

}
