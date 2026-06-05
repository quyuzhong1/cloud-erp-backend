package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * WEGO 入库订单创建/修改（interfaceType=inorder.save）请求参数
 *
 * <p>WEGO 同一接口承担「创建」与「修改」语义：</p>
 * <ul>
 *     <li>{@code no} 为空时执行新增，接口返回 WEGO 单号；</li>
 *     <li>{@code no} 非空时执行修改，需传入 WEGO 入库单号。</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class WegoInOrderSaveDTO implements Serializable {

    /**
     * 入库单创建/修改请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveReqDTO {

        /**
         * WEGO accessToken（取自 overseas_provider.auth_json.appToken）
         */
        @NotBlank(message = "accessToken不能为空")
        private String accessToken;

        /**
         * WEGO secret（仅参与本地签名，不会透传到第三方）
         */
        @NotBlank(message = "secret不能为空")
        private String secret;

        /**
         * WEGO 入库单号，为空时创建新单据；非空时修改对应单据
         */
        private String no;

        /**
         * 海外仓供应商，对应 WEGO warehouseBusiness 字段
         */
        @NotBlank(message = "warehouseBusiness不能为空")
        private String warehouseBusiness;

        /**
         * 仓库代码
         */
        @NotBlank(message = "warehouseCode不能为空")
        private String warehouseCode;

        /**
         * 到货方式：
         * <ul>
         *     <li>4 - 20FT(打板货)</li>
         *     <li>5 - 20FT(散装货)</li>
         *     <li>6 - 40FT(打板货)</li>
         *     <li>7 - 40FT(散装货)</li>
         *     <li>8 - 海运/空运散货</li>
         * </ul>
         */
        @NotBlank(message = "warehouseDelivery不能为空")
        private String warehouseDelivery;

        /**
         * 库存类型：0 - 2C库存；1 - 2B库存
         */
        @NotNull(message = "inventoryType不能为空")
        private Integer inventoryType;

        /**
         * 预计到货日期，格式 yyyy-MM-dd
         */
        private String expectedArrivalDate;

        /**
         * 跟踪号/货柜号
         */
        private String trackNumber;

        /**
         * 参照编号（第三方系统单号）
         */
        private String referenceNumber;

        /**
         * 备注
         */
        private String notes;

        /**
         * 入库产品明细
         */
        private List<Detail> details;
    }

    /**
     * 入库产品明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail implements Serializable {

        /**
         * 产品明细id（Long类型）。新增或删除时传 null，修改时取上次创建返回的值
         */
        private Long inOrderDetailId;

        /**
         * 箱数
         */
        private Integer boxQty;

        /**
         * 箱唛，为空时系统自动生成
         */
        private String boxLabel;

        /**
         * 外箱长 cm
         */
        private Integer boxLength;

        /**
         * 外箱宽 cm
         */
        private Integer boxWidth;

        /**
         * 外箱高 cm
         */
        private Integer boxHeight;

        /**
         * 外箱重 kg
         */
        private Integer boxWeight;

        /**
         * 是否删除：true=删除，false=不删除
         */
        private Boolean deletedFlag;

        /**
         * 箱内产品明细
         */
        private List<Product> products;
    }

    /**
     * 箱内产品明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Product implements Serializable {

        /**
         * 产品 SKU
         */
        private String sku;

        /**
         * 数量
         */
        private Integer qty;
    }
}
