package com.sdk.wms.wego.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * WEGO 入库订单分页查询（interfaceType=inorder.queryPage）响应结构。
 *
 * <p>响应结构示例：</p>
 * <pre>
 * {
 *   "success": true,
 *   "errorCode": 0,
 *   "errorMsg": "操作成功",
 *   "serverTime": 1765200076,
 *   "result": {
 *     "pageNum": 1,
 *     "pageSize": 10,
 *     "total": 99,
 *     "pages": 10,
 *     "list": [ { "no": "Q202501170030", "warehouseBusiness": "WEGO", ... } ],
 *     "emptyFlag": false
 *   }
 * }
 * </pre>
 *
 * <p>{@code result.list} 中每条入库单包含：</p>
 * <ul>
 *     <li>{@link InorderDTO#getDetails()}：订单维度的入库箱明细（创建时录入）；</li>
 *     <li>{@link InorderDTO#getInstocks()}：实际入库明细（仓库实际操作后回写）；</li>
 *     <li>{@link InorderDTO#getStatementList()}：对账/费用明细。</li>
 * </ul>
 *
 * <p>{@link InorderDTO#getStatus()} 订单状态枚举：</p>
 * <ol>
 *     <li>待入库</li>
 *     <li>作业中</li>
 *     <li>上架中</li>
 *     <li>已完结</li>
 *     <li>已取消</li>
 *     <li>已删除</li>
 *     <li>已驳回</li>
 *     <li>已签收/完结（接口实际返回值，按业务对照）</li>
 * </ol>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WegoInboundResp implements Serializable {

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
         * WEGO 入库单号
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
         * 到货方式（接口实际返回为字符串，如 "1"/"3"/"4"/"5"）
         */
        @JSONField(name = "warehouseDelivery")
        private String warehouseDelivery;

        /**
         * 库存类型：0 - 2C库存；1 - 2B库存
         */
        @JSONField(name = "inventoryType")
        private Integer inventoryType;

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
         * 订单状态：1待入库 / 2作业中 / 3上架中 / 4已完结 / 5已取消 / 6已删除 / 7已驳回 / 8已签收
         */
        @JSONField(name = "status")
        private Integer status;

        /**
         * 订单明细（创建时录入的箱体明细）
         */
        @JSONField(name = "details")
        private List<DetailDTO> details;

        /**
         * 实际入库明细（仓库实际操作后回写）
         */
        @JSONField(name = "instocks")
        private List<InstockDTO> instocks;

        /**
         * 对账/费用明细（暂以原始 JSON 形式承载，业务侧按需扩展）
         */
        @JSONField(name = "statementList")
        private List<Object> statementList;
    }

    /**
     * 订单明细：创建/修改入库单时录入的箱体信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailDTO implements Serializable {

        /**
         * 产品明细 id
         */
        @JSONField(name = "inOrderDetailId")
        private Long inOrderDetailId;

        /**
         * 箱数
         */
        @JSONField(name = "boxQty")
        private Integer boxQty;

        /**
         * 已入库箱数
         */
        @JSONField(name = "instockBoxQty")
        private Integer instockBoxQty;

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
         * 外箱长 cm
         */
        @JSONField(name = "boxLength")
        private BigDecimal boxLength;

        /**
         * 外箱宽 cm
         */
        @JSONField(name = "boxWidth")
        private BigDecimal boxWidth;

        /**
         * 外箱高 cm
         */
        @JSONField(name = "boxHeight")
        private BigDecimal boxHeight;

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

        /**
         * 是否已删除
         */
        @JSONField(name = "deletedFlag")
        private Boolean deletedFlag;
    }

    /**
     * 实际入库明细：仓库实际操作后回写的箱体信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InstockDTO implements Serializable {

        /**
         * 关联的订单明细 id
         */
        @JSONField(name = "inOrderDetailId")
        private Long inOrderDetailId;

        /**
         * 是否不良品
         */
        @JSONField(name = "defectiveProductFlag")
        private Boolean defectiveProductFlag;

        /**
         * 入库批次（yyyy-MM-dd）
         */
        @JSONField(name = "batch")
        private String batch;

        /**
         * 生产批次
         */
        @JSONField(name = "productionBatch")
        private String productionBatch;

        /**
         * 生产日期（yyyy-MM-dd）
         */
        @JSONField(name = "productionDate")
        private String productionDate;

        /**
         * 失效日期（yyyy-MM-dd）
         */
        @JSONField(name = "expirationDate")
        private String expirationDate;

        /**
         * 入库箱数
         */
        @JSONField(name = "boxQty")
        private Integer boxQty;

        /**
         * 上架箱数
         */
        @JSONField(name = "boxUpQty")
        private Integer boxUpQty;

        /**
         * sku数量
         */
        @JSONField(name = "skuQty")
        private Integer skuQty;

        /**
         * 箱唛
         */
        @JSONField(name = "boxLabel")
        private String boxLabel;

        /**
         * 外箱长 cm
         */
        @JSONField(name = "boxLength")
        private BigDecimal boxLength;

        /**
         * 外箱宽 cm
         */
        @JSONField(name = "boxWidth")
        private BigDecimal boxWidth;

        /**
         * 外箱高 cm
         */
        @JSONField(name = "boxHeight")
        private BigDecimal boxHeight;

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

        /**
         * 入库状态：0 - 待上架；2 - 已上架（按 WEGO 实际返回对照）
         */
        @JSONField(name = "status")
        private Integer status;

        /**
         * 是否已删除
         */
        @JSONField(name = "deletedFlag")
        private Boolean deletedFlag;

        /**
         * 上架操作员
         */
        @JSONField(name = "upUserName")
        private String upUserName;

        /**
         * 创建人
         */
        @JSONField(name = "createUserName")
        private String createUserName;

        /**
         * 更新时间（yyyy-MM-dd HH:mm:ss）
         */
        @JSONField(name = "updateTime")
        private String updateTime;

        /**
         * 创建时间（yyyy-MM-dd HH:mm:ss）
         */
        @JSONField(name = "createTime")
        private String createTime;
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
