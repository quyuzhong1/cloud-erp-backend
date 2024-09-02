package com.erp.model.wms.dto.third;

import com.common.business.dto.ReceiverDTO;
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
     * 平台订单号
     */
    private String platformCode;

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
    public static class ReceiverInfo implements ReceiverDTO {
        /**
         * 买家姓名
         */
        private String buyerName;
        /**
         * 收件人姓名
         */
        private String name;

        /**
         * 收件人联系方式
         */
        private String phone;

        /**
         * 收件人邮箱
         */
        private String email;

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
         * 区域
         */
        private String district;

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

        /**
         * 收件人税号
         */
        private String taxNumber;

        @Override
        public String getAddressFirst() {
            return address1;
        }

        @Override
        public void setAddressFirst(String addressFirst) {
            this.address1 = addressFirst;
        }

        @Override
        public String getTelNumber() {
            return phone;
        }

        @Override
        public void setTelNumber(String telNumber) {
            this.phone = telNumber;
        }

        @Override
        public String getZipCode() {
            return zipcode;
        }

        @Override
        public void setZipCode(String zipCode) {
            this.zipcode = zipCode;
        }

        @Override
        public String getContact() {
            return name;
        }

        @Override
        public void setContact(String contact) {
            this.name = contact;
        }
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
