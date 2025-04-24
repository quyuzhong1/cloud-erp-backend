package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 海外仓入库单详情请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Data
@NoArgsConstructor
public class OverseasWarehouseInboundDetailDTO implements Serializable {

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewListDTO {

        /**
         * 主键id
         */
        private String detailId;

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 单号
         */
        private String code;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 发货仓名称
         */
        private String deliveryWarehouseName;

        /**
         * 发货仓ID
         */
        private String deliveryWarehouseId;

        /**
         * 中转仓名称
         */
        private String transferWarehouseName;

        /**
         * 中转仓ID
         */
        private String transferWarehouseId;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * 目的仓ID
         */
        private String toWarehouseId;

        /**
         * 海外仓平台产品名称
         */
        private String platformProductName;

        /**
         * 海外仓平台SKU号
         */
        private String platformSkuNo;

        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 是否组合品：combination 组合 single 单品
         */
        private Boolean isCombination;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 收发差异
         */
        private Integer diffQty;

        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;

        /**
         * 签收状态：not=未签收，already=已签收
         */
        private String receiveStatus;

        /**
         * 签收类型：system=平台系统签收，manual=手动签收
         */
        private String receiveType;

        /**
         * 物流跟踪号
         */
        private String trackingNo;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 海外仓平台产品名称
         */
        private String platformProductName;

        /**
         * 海外仓平台SKU号
         */
        private String platformSkuNo;

        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 是否组合品：combination 组合 single 单品
         */
        private Boolean isCombination;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 收发差异
         */
        private Integer diffQty;

        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;

        /**
         * 签收状态：not=未签收，already=已签收
         */
        private String receiveStatus;

        /**
         * 签收类型：system=平台系统签收，manual=手动签收
         */
        private String receiveType;

        /**
         * 图片
         */
        private String imagesUrl;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主表id
         */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19, message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
         * 海外仓平台产品名称
         */
        @NotBlank(message = "海外仓平台产品名称不能为空")
        @Size(max = 255, message = "海外仓平台产品名称最大长度不能超过255位")
        private String platformProductName;

        /**
         * 海外仓平台SKU号
         */
        @NotBlank(message = "海外仓平台SKU号不能为空")
        @Size(max = 64, message = "海外仓平台SKU号最大长度不能超过64位")
        private String platformSkuNo;

        /**
         * ERP系统产品名称
         */
        @NotBlank(message = "ERP系统产品名称不能为空")
        @Size(max = 255, message = "ERP系统产品名称最大长度不能超过255位")
        private String productName;

        /**
         * ERP的SKU ID
         */
        @NotBlank(message = "ERP的SKU ID不能为空")
        @Size(max = 255, message = "ERP的SKU ID最大长度不能超过255位")
        private String skuId;

        /**
         * 是否组合品：combination 组合 single 单品
         */
        @NotNull(message = "是否组合品：combination 组合 single 单品不能为空")
        private Boolean isCombination;

        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        private Integer receiveQty;

        /**
         * 在途数量
         */
        @NotNull(message = "在途数量不能为空")
        private Integer transportQty;

        /**
         * 装箱数量
         */
        @NotNull(message = "装箱数量不能为空")
        private Integer packQty;

        /**
         * 收发差异
         */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;

        /**
         * 签收状态：not=未签收，already=已签收
         */
        @NotBlank(message = "签收状态：not=未签收，already=已签收不能为空")
        @Size(max = 64, message = "签收状态：not=未签收，already=已签收最大长度不能超过64位")
        private String receiveStatus;

        /**
         * 签收类型：system=平台系统签收，manual=手动签收
         */
        @NotBlank(message = "签收类型：system=平台系统签收，manual=手动签收不能为空")
        @Size(max = 64, message = "签收类型：system=平台系统签收，manual=手动签收最大长度不能超过64位")
        private String receiveType;


    }


}