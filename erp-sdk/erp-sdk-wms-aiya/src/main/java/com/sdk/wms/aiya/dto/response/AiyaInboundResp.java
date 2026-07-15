package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AIYA 入库订单分页查询（interfaceType=inorder.queryPage）响应结构（骨架）。
 * <p>
 * 参照 {@code WegoInboundResp} 搭建，外层统一为 success / errorCode / errorMsg / serverTime / result。
 * TODO：字段以 AIYA 官方文档为准，需按实际返回补充/调整。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AiyaInboundResp implements Serializable {

    /**
     * 是否成功
     */
    @JSONField(name = "success")
    private Boolean success;

    /**
     * 错误代码（成功时为 0）
     */
    @JSONField(name = "errorCode")
    private Integer errorCode;

    /**
     * 错误信息
     */
    @JSONField(name = "errorMsg")
    private String errorMsg;

    /**
     * 服务器时间戳（秒）
     */
    @JSONField(name = "serverTime")
    private Long serverTime;

    /**
     * 分页结果体
     */
    @JSONField(name = "result")
    private PageResultDTO result;

    /**
     * 分页结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageResultDTO implements Serializable {

        /**
         * 当前页
         */
        @JSONField(name = "pageNum")
        private Integer pageNum;

        /**
         * 每页数量
         */
        @JSONField(name = "pageSize")
        private Integer pageSize;

        /**
         * 总条数
         */
        @JSONField(name = "total")
        private Integer total;

        /**
         * 总页数
         */
        @JSONField(name = "pages")
        private Integer pages;

        /**
         * 入库单列表
         */
        @JSONField(name = "list")
        private List<InorderDTO> list;

        /**
         * 是否为空页
         */
        @JSONField(name = "emptyFlag")
        private Boolean emptyFlag;
    }

    /**
     * 入库单
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InorderDTO implements Serializable {

        /**
         * AIYA 入库单号
         */
        @JSONField(name = "no")
        private String no;

        /**
         * 海外仓供应商代码
         */
        @JSONField(name = "warehouseBusiness")
        private String warehouseBusiness;

        /**
         * 仓库代码
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 预计到货日期（yyyy-MM-dd）
         */
        @JSONField(name = "expectedArrivalDate")
        private String expectedArrivalDate;

        /**
         * 跟踪号/货柜号
         */
        @JSONField(name = "trackNumber")
        private String trackNumber;

        /**
         * 参照编号（第三方系统单号）
         */
        @JSONField(name = "referenceNumber")
        private String referenceNumber;

        /**
         * 备注
         */
        @JSONField(name = "notes")
        private String notes;

        /**
         * 订单状态（含义以 AIYA 文档为准）
         */
        @JSONField(name = "status")
        private Integer status;

        /**
         * 订单明细（创建时录入的箱体明细）
         */
        @JSONField(name = "details")
        private List<DetailDTO> details;
    }

    /**
     * 订单明细：创建/修改入库单时录入的箱体信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailDTO implements Serializable {

        /**
         * 箱数
         */
        @JSONField(name = "boxQty")
        private Integer boxQty;

        /**
         * 单箱内 SKU 总件数
         */
        @JSONField(name = "skuQty")
        private Integer skuQty;

        /**
         * 箱唛
         */
        @JSONField(name = "boxLabel")
        private String boxLabel;

        /**
         * 外箱重 kg
         */
        @JSONField(name = "boxWeight")
        private BigDecimal boxWeight;

        /**
         * 箱内产品明细
         */
        @JSONField(name = "products")
        private List<ProductDTO> products;
    }

    /**
     * 箱内产品明细
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDTO implements Serializable {

        /**
         * 产品 SKU
         */
        @JSONField(name = "sku")
        private String sku;

        /**
         * 数量
         */
        @JSONField(name = "qty")
        private Integer qty;
    }
}
