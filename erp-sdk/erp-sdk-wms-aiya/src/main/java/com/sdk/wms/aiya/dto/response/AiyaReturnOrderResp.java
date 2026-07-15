package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AIYA 退货订单分页查询（interfaceType=returnorder.queryPage）响应结构（骨架）。
 * <p>
 * 参照 {@code WegoReturnOrderResp} 搭建。
 * TODO：字段及订单状态枚举以 AIYA 官方文档为准，需按实际返回补充/调整。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiyaReturnOrderResp implements Serializable {

    /** 是否成功 */
    @JSONField(name = "success")
    private Boolean success;

    /** 错误代码（成功时为 0） */
    @JSONField(name = "errorCode")
    private Integer errorCode;

    /** 错误信息 */
    @JSONField(name = "errorMsg")
    private String errorMsg;

    /** 服务器时间戳（秒） */
    @JSONField(name = "serverTime")
    private Long serverTime;

    /** 分页结果体 */
    @JSONField(name = "result")
    private PageResultDTO result;

    /** 分页结果 */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageResultDTO implements Serializable {

        /** 当前页 */
        @JSONField(name = "pageNum")
        private Integer pageNum;

        /** 每页数量 */
        @JSONField(name = "pageSize")
        private Integer pageSize;

        /** 数据总数 */
        @JSONField(name = "total")
        private Integer total;

        /** 总页数 */
        @JSONField(name = "pages")
        private Integer pages;

        /** 订单列表 */
        @JSONField(name = "list")
        private List<ReturnOrderDTO> list;

        /** 数据是否为空 */
        @JSONField(name = "emptyFlag")
        private Boolean emptyFlag;
    }

    /** 退货订单 */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnOrderDTO implements Serializable {

        /** AIYA 退货单号（即第三方单据编号，用于去重） */
        @JSONField(name = "no")
        private String no;

        /** 订单日期（yyyy-MM-dd） */
        @JSONField(name = "date")
        private String date;

        /** 完结日期（yyyy-MM-dd，可为 null） */
        @JSONField(name = "finishDate")
        private String finishDate;

        /** 海外仓供应商代码 */
        @JSONField(name = "warehouseBusiness")
        private String warehouseBusiness;

        /** 仓库代码 */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /** 到仓日期（yyyy-MM-dd） */
        @JSONField(name = "arrivalDate")
        private String arrivalDate;

        /** 物流公司名称 */
        @JSONField(name = "logisticsName")
        private String logisticsName;

        /** 跟踪号（退货物流单号） */
        @JSONField(name = "trackNumber")
        private String trackNumber;

        /** 参照编号（客户参考单号） */
        @JSONField(name = "referenceNumber")
        private String referenceNumber;

        /** 订单状态（含义以 AIYA 文档为准） */
        @JSONField(name = "status")
        private Integer status;

        /** 预计入库产品明细 */
        @JSONField(name = "queryProducts")
        private List<SkuDTO> queryProducts;

        /** 实际入库产品明细 */
        @JSONField(name = "instockProducts")
        private List<SkuDTO> instockProducts;

        /** 总费用 */
        @JSONField(name = "totalPrice")
        private BigDecimal totalPrice;

        /** 客户备注 */
        @JSONField(name = "clientTips")
        private String clientTips;
    }

    /** SKU 明细（queryProducts / instockProducts 共用） */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuDTO implements Serializable {

        /** SKU 编号 */
        @JSONField(name = "sku")
        private String sku;

        /** 数量 */
        @JSONField(name = "qty")
        private Integer qty;
    }
}
