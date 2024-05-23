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
    private BigDecimal goodsCount;
    private String shopName;
    private String warehouseName;
    private BigDecimal actualRefundAmount;
    private String warehouseNo;
    private String shopRemark;
    private String nickName;
    private String customerName;
    private String stockinTime;
    private Byte status;
    private String checkTime;
    private String shopNo;
    @SerializedName("details_list")
    private List<OrderDetailInfoDto> detailList;

    @Getter
    @Setter
    public static class OrderDetailInfoDto {
        private BigDecimal num;
        private BigDecimal srcPrice;
        private BigDecimal price;
        private BigDecimal totalCost;
        private String remark;
        private BigDecimal rightNum;
        private Integer recId;
        private String goodsName;
        private String goodsNo;
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
