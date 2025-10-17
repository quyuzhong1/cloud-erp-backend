package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class CreateOrderRequest {
    private List<Order> orders;
    @Data
    @Builder
    public static class Order{
        private String sku;
        private Integer amount;
    }
}
