package com.erp.model.wms.dto.third.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 发货单号
     */
    private String referenceNo;

    /**
     * 交货方式
     */
    private String incomeType;

    /**
     * 入库单类型
     */
    private String transitType;

    /**
     * 目的仓库
     */
    private String warehouseCode;

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
     * 联系人，取姓+名
     */
    private String contacter;

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
     * 入库单创建时取0，发货单审核通过更新为1
     */
    private String verify;

    /**
     * 报关方式
     */
    private String customsType;

    /**
     * 交货方式
     */
    private String collectingService;

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

    /**
     * 快递单号
     */
    private String deliveryCode;

    /**
     * 预计揽收时间
     */
    private String collectingTime;

    /**
     * 是否自有税号清关
     */
    private Integer clearanceService;

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
         * 箱号
         */
        private Integer boxNo;

        /**
         * 对应箱号装箱数量
         */
        private Integer quantity;
    }
}
