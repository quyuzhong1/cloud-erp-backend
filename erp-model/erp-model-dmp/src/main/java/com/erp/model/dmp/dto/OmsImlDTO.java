package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

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
        @SerializedName("receiving_code")
        private String receivingCode;
        /**
         * 参考号
         */
        @SerializedName("reference_no")
        private String referenceNo;
        /**
         * 交货方式，0自送，1揽收
         */
        @SerializedName("income_type")
        private String incomeType;
        /**
         * 入库单状态 C:新建 W:头程在途 P:头程收货中 Z:转运中 G:目的仓库收货中 F:目的仓收货完成 E:完成上架 X:废弃
         */
        @SerializedName("receiving_status")
        private String receivingStatus;

        /**
         * 入库单说明
         */
        @SerializedName("receiving_desc")
        private String receivingDesc;
        /**
         * 目的仓库
         */
        @SerializedName("warehouse_code")
        private String warehouseCode;

        /**
         * 交货仓库
         */
        @SerializedName("transit_warehouse_code")
        private String transitWarehouseCode;

        /**
         * 运输方式
         */
        @SerializedName("shipping_method")
        private String shippingMethod;

        /**
         * 跟踪号
         */
        @SerializedName("tracking_number")
        private String trackingNumber;
        /**
         * 创建时间
         */
        @SerializedName("receiving_add_time")
        private LocalDateTime receivingAddTime;
        /**
         * 更新时间
         */
        @SerializedName("receiving_modify_time")
        private LocalDateTime receivingModifyTime;
        /**
         * 预计到达日期
         */
        @SerializedName("eta_date")
        private LocalDate etaDate;
        /**
         * 揽收支持的省ID， 参考getRegionForReceiving
         */
        @SerializedName("region_id_level0")
        private String regionIdLevel0;
        /**
         * 揽收支持的市ID， 参考getRegionForReceiving
         */
        @SerializedName("region_id_level1")
        private String regionIdLevel1;
        /**
         * 揽收支持的区ID， 参考getRegionForReceiving
         */
        @SerializedName("region_id_level2")
        private String regionIdLevel2;
        /**
         * 揽收地址
         */
        @SerializedName("street")
        private String street;
        /**
         * 联系人，交货方式为揽收时，必填
         */
        @SerializedName("contacter")
        private String contacter;
        /**
         * 联系电话，交货方式为揽收时，必填
         */
        @SerializedName("contact_phone")
        private String contactPhone;
        /**
         * 总箱数
         */
        @SerializedName("box_total")
        private String boxTotal;
        /**
         * 产品总数
         */
        @SerializedName("sku_total")
        private String skuTotal;
        /**
         * SKU种类
         */
        @SerializedName("sku_species")
        private String skuSpecies;
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
        @SerializedName("product_barcode")
        private String productBarcode;
        /**
         * 客户商品编码
         */
        @SerializedName("product_sku")
        private String productSku;
        /**
         * 箱号编码
         */
        @SerializedName("box_no")
        private Integer boxNo;
        /**
         * 参考箱号
         */
        @SerializedName("reference_box_no")
        private String referenceBoxNo;
        /**
         * 数量
         */
        private Integer quantity;
        /**
         * 收货数量
         */
        @SerializedName("received_quantity")
        private Integer receivedQuantity;
        /**
         * 上架数量
         */
        @SerializedName("putaway_quantity")
        private Integer putawayQuantity;

        /**
         * 上架数
         */
        @SerializedName("putaway_qty")
        private Integer putawayQty;

        @SerializedName("rd_update_time")
        private LocalDateTime rdUpdateTime;
        /**
         * 库位类型统计数量,0:良品,1:不良品,2:暂存
         */
        private List<Integer> loTypeCount;

    }

}
