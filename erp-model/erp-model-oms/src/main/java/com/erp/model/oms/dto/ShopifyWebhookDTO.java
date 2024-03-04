package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ShopifyWebhookDTO {

    /**
     * 请求查看存储的客户数据
     */
    @Data
    @NoArgsConstructor
    public static class CustomersDataRequestDTO {
        /**
         * 店铺id
         */
        private int shopId;
        /**
         * 域名
         */
        private String shopDomain;
        /**
         * 用户信息
         */
        private CustomerBean customer;
        /**
         * 请求信息
         */
        private DataRequestBean dataRequest;
        /**
         * 订单请求
         */
        private List<Integer> ordersRequested;
    }

    /**
     * 要求删除客户数据
     */
    @Data
    @NoArgsConstructor
    public static class CustomersRedactDTO {
        /**
         * 店铺id
         */
        private int shopId;
        /**
         * 域名
         */
        private String shopDomain;
        private CustomerBean customer;
        private List<Integer> ordersToRedact;
    }

    /**
     * 要求删除店铺数据
     */
    @Data
    @NoArgsConstructor
    public static class ShopRedactDTO {
        /**
         * 店铺id
         */
        private int shopId;
        /**
         * 域名
         */
        private String shopDomain;
    }
}
