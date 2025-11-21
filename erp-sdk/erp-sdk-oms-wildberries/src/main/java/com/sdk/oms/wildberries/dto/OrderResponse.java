package com.sdk.oms.wildberries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName OrderResponse
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderResponse extends BaseResponse{
    private Long next;
    private List<Order> orders;

    @Data
    public static class Order{

        private Address address;
        private BigDecimal scanPrice;
        private String deliveryType;
        private String supplyId;
        private String orderUid;
        private String article;
        private String colorCode;
        private String rid;
        private String createdAt;
        private List<String> offices;
        private List<String> skus;
        private Long id;
        private Long warehouseId;
        private Long officeId;
        private Long nmId;
        private Long chrtId;
        private BigDecimal price;
        private BigDecimal convertedPrice;
        private String currencyCode;
        private String convertedCurrencyCode;
        private Integer cargoType;
        private String comment;
        private Boolean isZeroOrder;

        private Option options;

    }

    @Data
    public static class Address{
        private String fullAddress;
        private BigDecimal longitude;
        private BigDecimal latitude;
    }
    @Data
    public static class Option{
        private Boolean isB2b;
    }
}
