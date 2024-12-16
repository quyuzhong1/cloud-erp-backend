package com.sdk.third.lingxing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 更新订单DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderDTO implements Serializable {

    @NotNull(message = "订单列表不能为空")
    private List<@NotNull(message = "订单信息不能为空") OrderInfo> orderList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo implements Serializable{
        /**
         * 收货信息
         */
        private AddressInfo addressInfo;

        /**
         * 全局系统单号
         */
        @NotNull(message = "全局系统单号不能为空")
        private Integer globalOrderNo;

        /**
         * 物流信息
         */
        private Logistics logistics;

        /**
         * 商品信息
         * 备注：【可以传空列表】
         */
        @NotNull(message = "商品信息不能为空")
        private List<OrderItem> orderItemList;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo implements Serializable {
        /**
         * 详细地址1
         */
        private String addressLine1;

        /**
         * 详细地址2
         */
        private String addressLine2;

        /**
         * 详细地址3
         */
        private String addressLine3;

        /**
         * 城市
         */
        private String city;

        /**
         * 区/县
         */
        private String district;

        /**
         * 门牌号
         */
        private String doorplateNo;

        /**
         * 邮编
         */
        private String postalCode;

        /**
         * 公司名
         */
        private String receiverCompanyName;

        /**
         * 国家/地区二字码
         */
        private String receiverCountryCode;

        /**
         * 手机
         */
        private String receiverMobile;

        /**
         * 收件人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String receiverTel;

        /**
         * 省/州
         */
        private String stateOrRegion;

        // Getters and Setters...
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem implements Serializable{
        /**
         * 商品备注
         */
        private String mark;

        /**
         * msku
         */
        private String msku;

        /**
         * 单价
         */
        private Integer price;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * sku
         */
        private String sku;

        /**
         * 编辑类型：1 新增，2 删除，3 覆盖
         */
        private Integer type;

        /**
         * 系统订单商品ID
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Logistics implements Serializable {
        /**
         * 是否COD订单（是 or 否）
         */
        private String codType;

        /**
         * 税号
         */
        private String senderTaxNo;

        /**
         * 税号类型（VAT/CPF/IOSS/EORI/收件人税号）
         */
        private String senderTaxType;
    }
}
