package com.erp.model.dmp.wdt;

import com.common.business.dto.CleanBaseDTO;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class WangDianReturnOrderEntity extends CleanBaseDTO {
    private String _id;
    private String orderNo;
    private String createdTime;
    private Integer stockinId;
    private String customerNo;
    private BigDecimal totalPrice;
    private String refundNo;
    private String tradeNoList;
    private String remark;
    private String reason;
    private BigDecimal goodsCount;
    private String warehouseId;
    private String warehouseName;
    private String warehouseNo;
    private String shopId;
    private String shopNo;
    private String shopName;
    private String tidList;
    private BigDecimal actualRefundAmount;
    private String shopRemark;
    private String nickName;
    private String customerName;
    private String stockinTime;
    private Byte status;
    private String checkTime;
    @SerializedName("details_list")
    private List<OrderDetailInfoDto> detailList;

    @Getter
    @Setter
    public static class OrderDetailInfoDto {
        private BigDecimal num;
        private BigDecimal srcPrice;
        private BigDecimal expectNum;
        private BigDecimal stockinNum;
        private BigDecimal price;
        private String refundDetailId;
        private BigDecimal totalCost;
        private String remark;
        private BigDecimal rightNum;
        private String recId;
        private String goodsName;
        private String goodsNo;
        private String positionNo;
        private String specNo;
        private String prop2;
        private String specName;
        private String specCode;
        private String brandNo;
        private String brandName;
        private String tradeNo;
        private String tradeType;
        private Integer srcOrderType;

    }

}
