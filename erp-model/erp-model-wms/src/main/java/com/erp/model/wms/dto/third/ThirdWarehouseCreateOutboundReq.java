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
     * 平台
     */
    private String platform;

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * erp销售订单号
     */
    private String soCode;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 尾程服务商
     */
    private String lastMileCarrier;

    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 货主编码
     */
    private String ownerCode;
    /**
     * 配送方式
     */
    private String shippingMethod;

    /**
     * 配送方式名称
     */
    private String shippingMethodName;

    /**
     * 配送方式Id
     */
    private String shippingMethodId;
    /**
     * 配送仓库
     */
    private String warehouseCode;

    /**
     * 是否审核 默认1
     */
    private Integer verify;

    /**
     * 是否线上订单
     */
    private boolean onlineFlag;

    /**
     * 面单base64数据
     */
    private String labelData;

    /**
     * 发票base64数据
     */
    private String invoiceData;

    /**
     * 线上订单物流单号
     */
    private String trackingNo;

    /**
     * 线上面单url
     */
    private String labelUrl;

    //配送商
    private String carrierType;

    //收件人信息
    private ReceiverInfo receiverInfo;

    /**
     * EORI税号
     */
    private String eoriTaxNo;

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
         * 买家电话
         */
        private String buyerNumber;
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
         * 海外仓产品SKU id
         */
        private String productSkuId;

        /**
         * 数量
         */
        private Integer quantity;

        private String skuId;

        private String skuNo;

        private String sourceSkuNo;

        private String sourceSkuId;

        private String platformDetailId;
        private String detailId;
        /**
         * 海关编码
         */
        private String hsCode;

        public Item(String productSku, Integer quantity,String hsCode,String productSkuId) {
            this.productSku = productSku;
            this.productSkuId = productSkuId;
            this.quantity = quantity;
            this.hsCode = hsCode;
        }
    }

    /**
     * 订单附件
     */
    private List<Attach> attach;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Attach {

        private String fileType;

        private Integer attachId;
    }
}
