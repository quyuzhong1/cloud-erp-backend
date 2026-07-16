package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AIYA（爱亚/百世 GLINK）创建/修改入库单（serviceType={@code GLINK_CREATE_ASN_NOTIFY}）请求报文。
 * <p>
 * 对应百世 GLINK ASN 接口的 {@code request} 根对象；{@code customerCode}（用户编码）为必填业务参数，
 * 由 {@code AiyaOpenApiService} 从授权信息统一注入 bizData，本 DTO 不重复承载。
 * <p>
 * 仅保留 ERP 侧实际会赋值的字段，其余 udf/预留字段按需扩展。fastjson 序列化默认忽略 null 字段，
 * 因此可选字段为空时不会出现在请求体中。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiyaInboundSaveDTO implements Serializable {

    /**
     * 入库单号（必填）：ERP 侧唯一号，创建/修改以此为幂等键。
     */
    private String asnNumber;

    /**
     * 客户单号
     */
    private String extAsnNumber;

    /**
     * 仓库编码（必填）
     */
    private String warehouseCode;

    /**
     * 需要告诉仓库操作人员的备注信息
     */
    private String warehouseNotes;

    /**
     * 承运商信息
     */
    private String carrier;

    /**
     * 参考号
     */
    private String refNumber;

    /**
     * 参考号
     */
    private String referenceNumber;

    /**
     * 运单号
     */
    private String trackingNumber;

    /**
     * 海运柜号
     */
    private String containerNumber;

    /**
     * 海运柜封签号
     */
    private String sealNumber;

    /**
     * 入库单类型：SUPPLIER_RECEIPT 供应商入库、RETURN 退货入库、CONTAINER 整柜入库、RELABEL 换标入库。
     */
    private String asnType;

    /**
     * 预计到货日期（yyyy-MM-dd）
     */
    private String expectedReceiptDate;

    /**
     * 入库单商品种类数
     */
    private Integer itemLineQty;

    /**
     * 箱数
     */
    private String cartonQty;

    /**
     * 箱信息
     */
    private List<MarkList> markList;

    /**
     * 发运信息明细（必填）
     */
    private List<AsnLineItem> asnLineItems;

    /**
     * 箱信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkList implements Serializable {
        /**
         * 箱号/箱唛
         */
        private String markCode;
        /**
         * 箱长
         */
        private BigDecimal length;
        /**
         * 箱宽
         */
        private BigDecimal width;
        /**
         * 箱高
         */
        private BigDecimal height;
        /**
         * 箱重量
         */
        private BigDecimal weight;
        /**
         * 长度单位
         */
        private String lengthUnit;
        /**
         * 重量单位
         */
        private String weightUnit;
    }

    /**
     * 发运信息明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsnLineItem implements Serializable {
        /**
         * 行号
         */
        private String lineNo;
        /**
         * 商品编码（必填）
         */
        private String sku;
        /**
         * 数量（必填）
         */
        private Integer quantity;
        /**
         * 箱唛（关联 markList.markCode）
         */
        private String markCode;
        /**
         * 良品-GOOD，不良品-DAMAGE
         */
        private String skuStatus;
        /**
         * 批次号
         */
        private String batchNo;
    }
}
