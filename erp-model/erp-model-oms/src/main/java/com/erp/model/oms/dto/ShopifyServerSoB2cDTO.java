package com.erp.model.oms.dto;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShopifyServerSoB2cDTO {

    @Data
    @NoArgsConstructor
    public static class SoB2cLogisticQueryDTO {

        /**
         * 运单号（方式一）
         */
        private String trackingNumber;

        /**
         * 订单号（方式二）
         */
        private String orderNumber;

        /**
         * 联系信息（方式二：邮箱或电话号码）
         */
        private String contactInfo;

        /**
         * 校验是否为方式二查询
         */
        public boolean isOrderNumberQuery() {
            return StringUtils.isNotBlank(orderNumber) && StringUtils.isNotBlank(contactInfo);
        }
    }

    /**
     * 订单物流信息DTO
     */
    @Data
    @NoArgsConstructor
    public static class SoB2cLogisticInfoDTO {

        private String code;
        private String orderNumber;
        private LocalDateTime lastSignTime;
        private String platformCode;
        private String billStatus;
        private String orderStatus;
        private OrderInfo orderInfo;
        private Logistics logistics;
        private List<Product> products;

    }

    @Data
    @NoArgsConstructor
    public static class OrderInfo {
        private LocalDateTime createdAt;
        private LocalDateTime packagedAt;
    }

    @Data
    @NoArgsConstructor
    public static class Logistics {
        private String carrier;
        private String trackingNumber;
        private String trackingUrl;
        private List<StatusUpdate> statusUpdates;
    }

    @Data
    @NoArgsConstructor
    public static class StatusUpdate {
        private String trackingId;
        private LocalDateTime timestamp;
        private String location;
        private String description;
        private String status;
    }

    @Data
    public static class Product {
        private String detailId;
        private String platformDetailId;
        private String platformSkuNo;
        private String platformSpuNo;
        private String productName;
        private String productImage;
    }
}
