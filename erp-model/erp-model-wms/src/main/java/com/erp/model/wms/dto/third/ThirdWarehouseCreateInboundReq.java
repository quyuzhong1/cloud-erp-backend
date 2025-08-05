package com.erp.model.wms.dto.third;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThirdWarehouseCreateInboundReq {

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 货主编码
     */
    private String ownerCode;

    /**
     * 第三方入库单号（编辑时必填）
     */
    private String receivingCode;

    /**
     * 发货单号
     */
    private String referenceNo;

    /**
     * file base64
     */
    private String fileBase64;
    /**
     * 交货方式 （自送，揽收）
     * {@link com.erp.model.wms.enums.OverseasDeliveryModeEnum}
     */
    private String incomeType;

    /**
     * 入库类型 （自发头程,中转代发）
     * {@link com.erp.model.wms.enums.OverseasInstockTypeEnum}
     */
    private String receivingType;

    /**
     * 入库单类型 （标准入库单，中转入库单(标准货运单)，FBA入库单）
     * {@link com.erp.model.wms.enums.OverseasInstockTypeEnum}
     */
    private String transitType;

    /**
     * 目的仓库
     */
    private String warehouseCode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 中转仓库
     */
    private String transitWarehouseCode;

    /**
     * 物流产品代码
     */
    private String smCode;

    /**
     * 物流方式
     * {@link com.erp.model.wms.enums.LogisticsMethodEnum}
     */
    private String receivingShippingType;
    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 预计到达日期
     */
    private LocalDateTime etaDate;

    /**
     * 揽收时间起
     */
    @TableField(value = "collect_start_time")
    private LocalDateTime collectStartTime;

    /**
     * 揽收时间止
     */
    @TableField(value = "collect_end_time")
    private LocalDateTime collectEndTime;
    /**
     * 入库单创建时取0，发货单审核通过更新为1
     * {@link com.common.business.enums.OverseasVerifyEnum}
     */
    private String verify;

    /**
     * 报关方式
     * {@link com.erp.model.wms.enums.OverseasCustomsTypeNewEnum}
     */
    private String customsType;

    /**
     * 报关类型
     */
    private String declareType;

    /**
     * 交货方式
     * {@link com.erp.model.wms.enums.OverseasDeliveryModeEnum}
     */
    private String collectingService;

    /**
     * 快递单号
     */
    private String deliveryCode;

    /**
     * 是否自有税号清关
     */
    private Integer clearanceService;

    //发货信息
    private ShiperInfo shiperInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ShiperInfo {

        //联系人姓名
        private String contacterName;

        //联系电话（手机号）
        private String phone;

        //发件国家/地区简称
        private String countryCode;

        //省/州名
        private String stateName;

        //城市名
        private String cityName;

        //区名
        private String region;

        //发货地址
        private String address1;

        //发货地址
        private String address2;
    }

    //揽收信息
    private Collect collect;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Collect {

        /**
         * 联系人姓+名
         */
        private String contacterName;

        /**
         * 联系人名
         */
        private String contacterFirstName;

        /**
         * 联系人姓
         */
        private String contacterLastName;

        /**
         * 联系电话
         */
        private String contactPhone;

        /**
         * 揽收支持的省ID
         */
        private String collectStateId;

        /**
         * 揽收支持的市ID
         */
        private String collectCityId;

        /**
         * 揽收支持的区ID
         */
        private String collectAreaId;

        /**
         * 揽收地址
         */
        private String collectStreet;

        /**
         * 揽收地址2
         */
        private String collectStreet2;

        /**
         * 预计揽收时间
         */
        private LocalDateTime collectingTime;

        /**
         * 揽收地址国家/地区
         */
        private String collectCountryCode;

        /**
         * 揽收省份的中文
         */
        private String collectStateName;

        /**
         * 揽收城市的中文
         */
        private String collectCityName;

        /**
         * 揽收地址邮编
         */
        private String collectZipcode;

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

        private String batchNo;
        /**
         * 海外仓产品SKU
         */
        private String productSku;

        /**
         * 海外仓产品SKUId
         */
        private String productSkuId;
        /**
         * 箱号
         */
        private Integer boxNo;

        /**
         * 对应箱号装箱数量
         */
        private Integer quantity;
        /**
         * 箱子尺寸（长）
         */
        private BigDecimal boxLength;
        /**
         * 箱子尺寸（宽）
         */
        private BigDecimal boxWidth;
        /**
         * 箱子尺寸（高）
         */
        private BigDecimal boxHeight;
        /**
         * 尺寸单位
         */
        private String sizeUnit;

        /**
         * 实际箱重（设备更新）
         */
        private BigDecimal packageWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String weightUnit;
    }
}
