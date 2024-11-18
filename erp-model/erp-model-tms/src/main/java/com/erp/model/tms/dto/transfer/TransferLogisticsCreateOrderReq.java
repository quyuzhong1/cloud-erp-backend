package com.erp.model.tms.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 * 默认集货模式，自有渠道,现在只对接保宏，仓库默认sz01，
 */
@Data
@AllArgsConstructor
@Builder
public class TransferLogisticsCreateOrderReq {

    /**
     * 服务商单号,必填
     */
    @NotBlank(message = "服务商单号不能为空")
    private String trackingNumber;

    /**
     * 收件人国家
     */
    @NotBlank(message = "收件人国家不能为空")
    private String country;

    /**
     * 运输方式代码
     */
    @NotBlank(message = "运输方式代码不能为空")
    private String shippingCode;

    /**
     * 收件人姓名
     */
    @NotBlank(message = "收件人姓名不能为空")
    private String name;

    /**
     * 订单参考号
     */
    @NotBlank(message = "订单参考号不能为空")
    private String referenceNo;

    /**
     * 派送地址
     */
    @NotBlank(message = "派送地址不能为空")
    private String deliveryAddress;

    /**
     * 收件人地址1
     */
    @NotBlank(message = "收件人地址1不能为空")
    private String streetAddress;

    /**
     * 收件人地址2
     */
    private String streetAddress2;

    /**
     * 收件人州/区域
     */
    private String state;

    /**
     * 收件人城市
     */
    private String city;

    /**
     * 收件人邮编
     */
    private String postcode;

    /**
     * 收件人电话
     */
    private String phone;

    /**
     * 提交订单状态1,草稿;2,确认;
     */
    private String orderStatus;

    /**
     * IOSS 号
     */
    private String iossNo;

    /**
     *平台订单号
     */
    private String serialNo;

    /**
     * 订单包裹重量
     */
    private BigDecimal grossWeight;

    /**
     * 是否购买运单保险。0：不购买； 1：购买
     */
    private Integer buyInsurance;

    /**
     * 产品信息
     */
    @NotNull(message = "产品信息不能为空")
    @Valid
    private List<ProductDetail> productDetailList;


    @Data
    @AllArgsConstructor
    @Builder
    public static class ProductDetail {

        /**
         * 平台sku
         */
        @NotNull(message = "sku不能为空")
        private String skuNo;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
         * 英文品名
         */
        protected String productTitleEn;


        /**
         * 目的海关申报单价
         */
        protected String purposeDeclaredValue;
    }
}
