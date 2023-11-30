package com.erp.model.wms.dto.third.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdWarehouseCreateOutboundReq extends ThirdWarehouseAuth {

    /**
     * 订单参考号
     */
    private String referenceNo;

    /**
     * 配送方式
     */
    private String shippingMethod;

    /**
     * 配送仓库
     */
    private String warehouseCode;

    /**
     * 是否审核 默认1
     */
    private Integer verify;

    //收件人信息
    private ReceiverInfo receiverInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceiverInfo {
        /**
         * 收件人姓名
         */
        private String name;

        /**
         * 收件人联系方式
         */
        private String phone;

        /**
         * 收件人国家
         */
        private String countryCode;

        /**
         * 省
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 邮编
         */
        private String zipcode;

        /**
         * 地址1
         */
        private String address1;

        /**
         * 地址2
         */
        private String address2;

        /**
         * 地址3
         */
        private String address3;

    }

    /**
     * 入库单明细
     */
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {

        /**
         * 海外仓产品SKU
         */
        private String productSku;

        /**
         * 数量
         */
        private Integer quantity;
    }
}
