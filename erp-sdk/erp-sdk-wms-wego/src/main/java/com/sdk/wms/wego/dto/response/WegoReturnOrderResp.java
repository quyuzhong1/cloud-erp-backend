package com.sdk.wms.wego.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * WEGO 退货订单分页查询（interfaceType=returnorder.queryPage）响应结构。
 *
 * <p>响应结构示例：</p>
 * <pre>
 * {
 *   "success": true,
 *   "errorCode": 0,
 *   "errorMsg": "操作成功",
 *   "serverTime": 1778319970,
 *   "result": {
 *     "pageNum": 1, "pageSize": 10, "total": 22, "pages": 3, "emptyFlag": false,
 *     "list": [ { "no": "R202512310001", "warehouseCode": "WG01", "status": 6, ... } ]
 *   }
 * }
 * </pre>
 *
 * <p>订单状态枚举（{@link ReturnOrderDTO#getStatus()}）：</p>
 * <ul>
 *   <li>1 - 待确认</li>
 *   <li>2 - 待收货</li>
 *   <li>3 - 待处理</li>
 *   <li>6 - 已处理（定时任务仅同步此状态）</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WegoReturnOrderResp implements Serializable {

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

        /** WEGO 退货单号（即第三方单据编号，用于去重） */
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

        /**
         * 库存类型：0-2C库存；1-2B库存；3-不良品
         */
        @JSONField(name = "inventoryType")
        private Integer inventoryType;

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

        /**
         * 订单状态：1-待确认；2-待收货；3-待处理；6-已处理
         */
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

        /** 包裹长 cm */
        @JSONField(name = "boxLength")
        private BigDecimal boxLength;

        /** 包裹宽 cm */
        @JSONField(name = "boxWidth")
        private BigDecimal boxWidth;

        /** 包裹高 cm */
        @JSONField(name = "boxHeight")
        private BigDecimal boxHeight;

        /** 包裹重量 kg */
        @JSONField(name = "boxWeight")
        private BigDecimal boxWeight;

        /** 体积 m³ */
        @JSONField(name = "cbm")
        private BigDecimal cbm;

        /**
         * 到仓方式：31-快递类小件；32-快递类大件；33-托盘
         */
        @JSONField(name = "deliveryType")
        private String deliveryType;

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
