package com.sdk.wms.jitu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 极兔出库单创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOutOrderCreateRequest implements Serializable {

    /**
     * 货主编号
     */
    private String customerid;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 货主ID
     */
    private String eccompanyid;

    /**
     * 客户订单号
     */
    private String txlogisticid;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 来源平台
     */
    private String source;

    /**
     * 平台单号
     */
    private String platformNumber;

    /**
     * 付款时间
     */
    private String payTime;

    /**
     * 订单标识
     */
    private String businessMode;

    /**
     * 包裹号
     */
    private String packageId;

    /**
     * 物流服务商ID
     */
    private String shippingProviderid;

    /**
     * 收件人信息
     */
    private Receiver receiver;

    /**
     * 配送方式
     */
    private String transportMode;

    /**
     * 物流公司编码
     */
    private String carrier;

    /**
     * 物流产品
     */
    private String routeid;

    /**
     * 承运单号
     */
    private String mailno;

    /**
     * 面单URL
     */
    private String label;

    /**
     * 卖家备注
     */
    private String deliveryNote;

    /**
     * 是否代收货款
     */
    private String isCod;

    /**
     * 店铺编码
     */
    private String storeCode;

    /**
     * 仓库编码
     */
    private String storerKey;

    /**
     * 来源系统
     */
    private String sourceSystem;

    /**
     * 外部业务单号
     */
    private String outBizNo;

    /**
     * 创建订单时间
     */
    private String createOrderTime;

    /**
     * 发件人信息
     */
    private Sender sender;

    /**
     * 物流名称
     */
    private String logisticsName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 商品总价值
     */
    private BigDecimal itemsvalue;

    /**
     * 币种
     */
    private String pricecurrency;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 重量
     */
    private Integer weight;

    /**
     * 商品信息列表
     */
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Receiver {
        /**
         * 收货国家
         */
        private String countrycode;

        /**
         * 短地址
         */
        private String shortAddress;

        /**
         * 地址1
         */
        private String address;

        /**
         * 地址2
         */
        private String address2;

        /**
         * 收货区县
         */
        private String area;

        /**
         * 收货城市
         */
        private String city;

        /**
         * 收货省份
         */
        private String prov;

        /**
         * 收货邮编
         */
        private String postcode;

        /**
         * 收货人名称
         */
        private String name;

        /**
         * 收货人电话
         */
        private String phone;

        /**
         * 收货人手机
         */
        private String mobile;

        /**
         * 门牌号
         */
        private String doorNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sender {
        /**
         * 收货国家
         */
        private String countrycode;

        /**
         * 公司名称
         */
        private String company;

        /**
         * 发件人姓名
         */
        private String name;

        /**
         * 邮编
         */
        private String postcode;

        /**
         * 邮箱
         */
        private String mailbox;

        /**
         * 省份
         */
        private String prov;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String area;

        /**
         * 地址1
         */
        private String address;

        /**
         * 地址2
         */
        private String address2;

        /**
         * 手机
         */
        private String mobile;

        /**
         * 电话
         */
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        /**
         * 行号
         */
        private Integer lineNo;

        /**
         * 货品编码
         */
        private String itemCode;

        /**
         * 计划数量
         */
        private Integer number;

        /**
         * 标价
         */
        private BigDecimal itemvalue;

        /**
         * 库存类型
         */
        private String inventoryType;

        /**
         * 平台商品明细id
         */
        private String skuId;

        /**
         * 批次号
         */
        private String batchCode;

        /**
         * 生产编码
         */
        private String produceCode;

        /**
         * 生产日期
         */
        private String productDate;

        /**
         * 是否赠品
         */
        private String isGift;
    }
}