package com.sdk.oms.wildberries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderStatusResponse extends BaseResponse{
    private List<OrderStatus> orders;

    @Data
    public static class OrderStatus{
        private Long id;
        private String supplierStatus;
        private String wbStatus;
    }
}
