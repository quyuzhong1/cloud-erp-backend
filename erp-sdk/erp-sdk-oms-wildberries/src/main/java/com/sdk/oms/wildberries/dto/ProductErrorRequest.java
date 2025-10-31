package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SkuRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class ProductErrorRequest implements Serializable {

    private Cursor cursor;
    private Order order;

    @Data
    @Builder
    public static class Order{
        private Boolean ascending;
    }

    @Data
    @Builder
    public static class Cursor{
        private Integer limit;
        private String updatedAt;
        private String batchUUID;
    }
}
