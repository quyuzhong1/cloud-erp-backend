package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 艾姆勒返回数据格式
 *
 * @Author Cloud
 * @Date 2023/3/29 10:45
 **/
public class OmsImlDTO {

    @Data
    @ToString
    public static class MessageDTO{
        private String _id;
        /**
         * 入库单号
         */
        @JsonProperty("receiving_code")
        private String receivingCode;
        /**
         * 参考号
         */
        @JsonProperty("reference_no")
        private String referenceNo;
        /**
         * 交货方式，0自送，1揽收
         */
        @JsonProperty("income_type")
        private String incomeType;
        /**
         * 入库单状态 C:新建 W:头程在途 P:头程收货中 Z:转运中 G:目的仓库收货中 F:目的仓收货完成 E:完成上架 X:废弃
         */
        @JsonProperty("receiving_status")
        private String receivingStatus;

        /**
         * 入库单说明
         */
        @JsonProperty("receiving_desc")
        private String receivingDesc;
        /**
         * 目的仓库
         */
        @JsonProperty("warehouse_code")
        private String warehouseCode;

        /**
         * 交货仓库
         */
        @JsonProperty("transit_warehouse_code")
        private String transitWarehouseCode;

        /**
         * 运输方式
         */
        @JsonProperty("shipping_method")
        private String shippingMethod;

        /**
         * 跟踪号
         */
        @JsonProperty("tracking_number")
        private String trackingNumber;
        /**
         * 创建时间
         */
        @JsonProperty("receiving_add_time")
        private LocalDateTime addTime;
        /**
         * 更新时间
         */
        @JsonProperty("receiving_update_time")
        private LocalDateTime updateTime;
        /**
         * 预计到达日期
         */
        @JsonProperty("eta_date")
        private LocalDate etaDate;
        /**
         * 揽收支持的省ID， 参考getRegionForReceiving
         */
        @JsonProperty("region_id_level0")
        private Integer regionIdLevel0;
        /**
         * 揽收支持的市ID， 参考getRegionForReceiving
         */
        @JsonProperty("region_id_level1")
        private Integer regionIdLevel1;
        /**
         * 揽收支持的区ID， 参考getRegionForReceiving
         */
        @JsonProperty("region_id_level2")
        private Integer regionIdLevel2;
        /**
         * 揽收地址
         */
        @JsonProperty("street")
        private String street;
        /**
         * 联系人，交货方式为揽收时，必填
         */
        @JsonProperty("contacter")
        private String contacter;
        /**
         * 联系电话，交货方式为揽收时，必填
         */
        @JsonProperty("contact_phone")
        private String contactPhone;
        /**
         * 总箱数
         */
        @JsonProperty("box_total")
        private Integer boxTotal;
        /**
         * 产品总数
         */
        @JsonProperty("sku_total")
        private Integer skuTotal;
        /**
         * SKU种类
         */
        @JsonProperty("sku_species")
        private Integer skuSpecies;
        /**
         * 入库明细
         */
        private List<ReceivingDetailDTO> items;

        /**
         * 重试次数
         */
        private Integer retry = 1;
    }

    /**
     * 入库明细
     */
    @Data
    @ToString
    public static class ReceivingDetailDTO{
        /**
         * 商品编码 唯一
         */
        @JsonProperty("product_barcode")
        private String productBarcode;
        /**
         * 客户商品编码
         */
        @JsonProperty("product_sku")
        private String productSku;
        /**
         * 箱号编码
         */
        @JsonProperty("box_no")
        private Integer boxNo;
        /**
         * 参考箱号
         */
        @JsonProperty("reference_box_no")
        private String referenceBoxNo;
        /**
         * 数量
         */
        private Integer quantity;
        /**
         * 收货数量
         */
        @JsonProperty("received_quantity")
        private Integer receiptQty;
        /**
         * 上架数量
         */
        @JsonProperty("putaway_quantity")
        private Integer putAwayQuantity;

        /**
         * 上架数
         */
        @JsonProperty("putaway_qty")
        private Integer putAwayQty;

        @JsonProperty("rd_update_time")
        private LocalDateTime updateTime;
        /**
         * 库位类型统计数量,0:良品,1:不良品,2:暂存
         */
        private List<Integer> loCountType;

    }

}
