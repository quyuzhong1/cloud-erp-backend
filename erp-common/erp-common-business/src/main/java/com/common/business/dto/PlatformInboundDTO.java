package com.common.business.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台入库单DTO,所有平台订单通用数据，转换为此类后发送mq统一消费处理
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformInboundDTO extends UniqueDto {

    /**
     * erp授权Id
     */
    private String authId;

    /**
     * 仓库平台类型
     * {@link WarehousePlatformTypeEnum}
     */
    private String warehousePlatformType;
    /**
     * 供应商
     * {@link OmsPlatformEnum}
     */
    private String provider;

    //入库单号
    private String receivingCode;

    //来源单号
    private String sourceCode;
    //ERP入库单状态
    private String receivingStatus;

    //下载时间
    private LocalDateTime downloadTime;
    //出库时间
    private LocalDateTime dateShipping;

    //入库明细
    private List<Item> items;

    @Data
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        /**
         * 产品条码
         */
        private String thirdBarcode;

        //SKU
        private String productSku;

        //收货数
        private Integer receivedQuantity;

        //上架数量
        private Integer putawayQuantity;

        //箱号
        @EqualsAndHashCode.Exclude
        private String boxNo;

        public Item(String productSku, Integer receivedQuantity) {
            this.productSku = productSku;
            this.receivedQuantity = receivedQuantity;
        }
    }

    //是否有签收数据,默认没有
    private Boolean hasReceivedData = false;

    //签收数据
    private List<Receiving> receivingDataList;

    @Data
    @ToString
    @EqualsAndHashCode
    public static class Receiving {

        //SKU
        private String productSku;

        //签收数量
        private Integer receiveQty;

        //签收人
        @EqualsAndHashCode.Exclude
        private String receiveUser;

        //签收时间
        private LocalDateTime receiveTime;

        //签收水流明细ID
        private String thirdId;

        //是否不良品 true 是 false 否（WEGO 等支持不良品标记的平台使用，其他平台留空即可）
        private Boolean defectiveProductFlag;

    }

}
