package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RefundStockinResponse {
    /*
{
	"status": 0,
	"data": {
		"total_count": 66,
		"order": [{
			"order_no": "RK1909180009",
			"details_list": [{
				"stockin_id": 1587,
				"num": 1.0000,
				"src_price": 5.0000,
				"price": 5.0000,
				"total_cost": 2.0000,
				"remark": "",
				"right_num": 1.0000,
				"rec_id": 2232,
				"goods_name": "张钟墙的苹果",
				"goods_no": "zzqdpg",
				"spec_no": "zzqdpg",
				"prop2": "",
				"spec_name": "111111111",
				"spec_code": "",
				"brand_no": "BRAND",
				"brand_name": "无",
				"trade_no": "JY201908260009",
				"trade_type": "线下订单"
			}],
			"created_time": 1568802721000,
			"stockin_id": 1587,
			"customer_no": "KH201809200001",
			"total_price": 2.0000,
			"refund_no": "TK1909180004",
			"trade_no_list": "JY201908260009",
			"remark": "----ZS201908260008",
			"goods_count": 1.0000,
			"shop_name": "张钟墙的店铺",
			"warehouse_name": "张钟墙的仓库",
			"actual_refund_amount": 5.0000,
			"warehouse_no": "960430",
			"shop_remark": "",
			"nick_name": "张钟墙",
			"customer_name": "",
			"stockin_time": 1568802722000,
			"status": 80,
			"check_time": 1568802721000,
			"shop_no": "960430"
		}]
	}
}
     */

    @SerializedName("total_count")
    private Integer total;
    @SerializedName("order")
    private List<OrderInfoDto> orders;

    @Getter
    @Setter
    public static class OrderInfoDto {
        private String orderNo;
        private String createdTime;
        private String stockinId;
        private String customerNo;
        private BigDecimal totalPrice;
        private String refundNo;
        private String tradeNoList;
        private String remark;
        private String reason;
        private BigDecimal goodsCount;
        @SerializedName("warehouse_id")
        private String warehouseId;
        @SerializedName("shop_id")
        private String shopId;
        private String shopName;
        private String warehouseName;
        private BigDecimal actualRefundAmount;
        private String warehouseNo;
        private String shopRemark;
        private String nickName;
        private String customerName;
        private String stockinTime;
        @SerializedName("tid_list")
        private String tidList;
        private String status;
        private String checkTime;
        private String shopNo;
        @SerializedName("details_list")
        private List<OrderDetailInfoDto> detailList;
    }
    @Getter
    @Setter
    public static class OrderDetailInfoDto {
        @SerializedName("stockin_id")
        private String stockinId;
        @SerializedName("refund_detail_id")
        private String refundDetailId;
        private BigDecimal num;
        private BigDecimal srcPrice;
        private BigDecimal price;
        private BigDecimal totalCost;
        private String remark;
        private BigDecimal rightNum;
        private String recId;
        private String goodsName;
        private String goodsNo;
        private String specNo;
        @SerializedName("position_no")
        private String positionNo;
        private String prop2;
        private String specName;
        private String specCode;
        private String brandNo;
        private String brandName;
        private String tradeNo;
        private String tradeType;
        private Integer srcOrderType;
        @SerializedName("expect_num")
        private BigDecimal expectNum;
        @SerializedName("stockin_num")
        private BigDecimal stockinNum;
    }
}
