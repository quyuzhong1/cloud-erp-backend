package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AIYA 2C 出库单查询响应通用结构（适用于 2c.order.search 与 2c.order.queryPage）（骨架）。
 * <p>
 * 参照 {@code WegoOutboundResp} 搭建。{@code 2c.order.search} 的 {@code result} 为数组，
 * 由 {@link com.sdk.wms.aiya.service.AiyaOpenApiService#search2cOrder} 直接解包为 List 返回，
 * 不在此类中映射，以避免 FastJSON 同名字段冲突。
 * TODO：字段及状态枚举以 AIYA 官方文档为准，需按实际返回补充/调整。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiyaOutboundResp implements Serializable {

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
     * 2c.order.queryPage 分页结果体
     */
    @JSONField(name = "result")
    private PageResultDTO result;

    /**
     * 分页结果（2c.order.queryPage 专用）
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
         * 是否为空页
         */
        @JSONField(name = "emptyFlag")
        private Boolean emptyFlag;

        /**
         * 2C 出库单列表
         */
        @JSONField(name = "list")
        private List<OutboundOrderDTO> list;
    }

    /**
     * 2C 出库单
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OutboundOrderDTO implements Serializable {

        /**
         * AIYA 2C 出库单号
         */
        @JSONField(name = "no")
        private String no;

        /**
         * 参照编号（创建时传入的 referenceCode）
         */
        @JSONField(name = "referenceCode")
        private String referenceCode;

        /**
         * 仓库编码
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 订单日期
         */
        @JSONField(name = "orderDate")
        private String orderDate;

        /**
         * 完结日期（出库完成后回填）
         */
        @JSONField(name = "finishDate")
        private String finishDate;

        /**
         * 订单状态（含义以 AIYA 文档为准）
         */
        @JSONField(name = "orderStatus")
        private Integer orderStatus;

        /**
         * 产品明细
         */
        @JSONField(name = "products")
        private List<ProductDTO> products;

        /**
         * 订单备注
         */
        @JSONField(name = "remark")
        private String remark;

        /**
         * 错误信息（提交失败或出库异常时回填）
         */
        @JSONField(name = "errorMessage")
        private String errorMessage;

        /**
         * 派送渠道名称
         */
        @JSONField(name = "logisticsName")
        private String logisticsName;

        /**
         * 物流状态（含义以 AIYA 文档为准）
         */
        @JSONField(name = "logisticsStatus")
        private Integer logisticsStatus;

        /**
         * 订单费用
         */
        @JSONField(name = "orderFee")
        private BigDecimal orderFee;

        /**
         * 创建时间
         */
        @JSONField(name = "createTime")
        private String createTime;

        /**
         * 派送明细（包含各包裹的物流跟踪号，取第一条非空 trackingNum 即为主跟踪号）
         */
        @JSONField(name = "logisticsList")
        private List<LogisticsDTO> logisticsList;
    }

    /**
     * 产品明细（单 SKU 行）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDTO implements Serializable {

        /**
         * 海外仓平台 SKU 编码
         */
        @JSONField(name = "sku")
        private String sku;

        /**
         * 发货数量
         */
        @JSONField(name = "qty")
        private Integer qty;
    }

    /**
     * 派送明细（每个包裹一条，含物流跟踪号）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogisticsDTO implements Serializable {

        /**
         * 派送渠道名
         */
        @JSONField(name = "logisticsName")
        private String logisticsName;

        /**
         * 物流跟踪号（即 ERP trackNo，出库后由 AIYA 回填）
         */
        @JSONField(name = "trackingNum")
        private String trackingNum;
    }
}
