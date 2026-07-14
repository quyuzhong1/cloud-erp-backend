package com.sdk.wms.wego.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * WEGO 2C 出库单查询响应通用结构（适用于 2c.order.search 与 2c.order.queryPage）。
 *
 * <p>{@code 2c.order.search} 响应示例：</p>
 * <pre>
 * {
 *   "success": true, "errorCode": 0, "errorMsg": "操作成功", "serverTime": 1765200076,
 *   "result": [
 *     {
 *       "no": "C2025051700026",
 *       "referenceCode": "WFHD20250517001",
 *       "warehouseCode": "BOS01",
 *       "orderStatus": 10,
 *       "logisticsList": [{"logisticsName": "UPS", "trackingNum": "1Z999AA1012345678"}],
 *       ...
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>{@code 2c.order.queryPage} 的 {@code result} 为分页对象（{@link PageResultDTO}），
 * {@link PageResultDTO#getList()} 内每条记录与 {@link OutboundOrderDTO} 完全一致，
 * 但额外包含 {@link OutboundOrderDTO#getStatementList()} 费用明细（search 接口无此字段）。</p>
 *
 * <p>{@link OutboundOrderDTO#getOrderStatus()} 状态枚举（来自 WEGO 官方文档）：</p>
 * <ul>
 *     <li>0  - 草稿</li>
 *     <li>1  - 提交失败（对应 ERP exception）</li>
 *     <li>2  - 已提交（对应 ERP waitShipped）</li>
 *     <li>3  - 拣货中（对应 ERP waitShipped）</li>
 *     <li>4  - 已拣货（对应 ERP waitShipped）</li>
 *     <li>10 - 已出库（对应 ERP shipped）</li>
 *     <li>11 - 已签收（对应 ERP shipped）</li>
 *     <li>13 - 出库异常（对应 ERP exception）</li>
 *     <li>15 - 已取消（对应 ERP disuse）</li>
 * </ul>
 *
 * <p>{@link OutboundOrderDTO#getLogisticsStatus()} 物流状态枚举（来自 WEGO 官方文档）：</p>
 * <ul>
 *     <li>20 - 查询不到</li>
 *     <li>30 - 运输中</li>
 *     <li>50 - 成功签收</li>
 *     <li>60 - 退货完成</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WegoOutboundResp implements Serializable {

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
     * 2c.order.queryPage 分页结果体；2c.order.search 的 result 是数组，由
     * {@link com.sdk.wms.wego.service.WegoOpenApiService#search2cOrder} 直接解包为 List 返回，
     * 不在此类中映射，以避免 FastJSON 同名字段冲突。
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
     * 2C 出库单（字段以 2c.order.queryPage 官方文档为准，search 接口字段是其子集）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OutboundOrderDTO implements Serializable {

        /**
         * WEGO 2C 出库单号
         */
        @JSONField(name = "no")
        private String no;

        /**
         * 参照编号（创建时传入的 referenceCode，对应 ERP 的 WFHD 三方仓发货单号）
         */
        @JSONField(name = "referenceCode")
        private String referenceCode;

        /**
         * 参照编号2（备用关联字段）
         */
        @JSONField(name = "referenceCode2")
        private String referenceCode2;

        /**
         * 海外仓供应商名称
         */
        @JSONField(name = "warehouseBusiness")
        private String warehouseBusiness;

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
         * 订单状态：0=草稿 1=提交失败 2=已提交 3=拣货中 4=已拣货 10=已出库 11=已签收 13=出库异常 15=已取消
         */
        @JSONField(name = "orderStatus")
        private Integer orderStatus;

        /**
         * 店铺名
         */
        @JSONField(name = "shopName")
        private String shopName;

        /**
         * 收货人姓名
         */
        @JSONField(name = "receiver")
        private String receiver;

        /**
         * 收货人电话
         */
        @JSONField(name = "receiverPhone")
        private String receiverPhone;

        /**
         * 收货人邮编
         */
        @JSONField(name = "receiverPostCode")
        private String receiverPostCode;

        /**
         * 收货人邮箱
         */
        @JSONField(name = "receiverEmail")
        private String receiverEmail;

        /**
         * 收货人省/州
         */
        @JSONField(name = "receiverProvince")
        private String receiverProvince;

        /**
         * 收货人城市
         */
        @JSONField(name = "receiverCity")
        private String receiverCity;

        /**
         * 收货人区
         */
        @JSONField(name = "receiverArea")
        private String receiverArea;

        /**
         * 收货地址
         */
        @JSONField(name = "receiverAddress")
        private String receiverAddress;

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
         * 发货人
         */
        @JSONField(name = "sender")
        private String sender;

        /**
         * 发货人电话
         */
        @JSONField(name = "senderPhone")
        private String senderPhone;

        /**
         * 发货人邮件
         */
        @JSONField(name = "senderEmail")
        private String senderEmail;

        /**
         * 指定收货日期（YYYY-MM-DD）
         */
        @JSONField(name = "receiveDate")
        private String receiveDate;

        /**
         * 指定收货时间段
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;

        /**
         * 需要派送：0=需要 1=不需要
         */
        @JSONField(name = "needSendFlag")
        private Integer needSendFlag;

        /**
         * 需要包装：0=需要 1=不需要
         */
        @JSONField(name = "needPackFlag")
        private Integer needPackFlag;

        /**
         * 面单类型：0=海外仓面单 1=平台指定面单
         */
        @JSONField(name = "wayBillType")
        private Integer wayBillType;

        /**
         * 平台指定面单 URL 列表（wayBillType=1 时有值）
         */
        @JSONField(name = "wayBillUrl")
        private List<String> wayBillUrl;

        /**
         * 派送渠道名称
         */
        @JSONField(name = "logisticsName")
        private String logisticsName;

        /**
         * 物流状态：20=查询不到 30=运输中 50=成功签收 60=退货完成
         */
        @JSONField(name = "logisticsStatus")
        private Integer logisticsStatus;

        /**
         * 其他物流状态描述（WEGO 扩展字段，含义以实际返回为准）
         */
        @JSONField(name = "logisticsOtherStatusName")
        private String logisticsOtherStatusName;

        /**
         * 运单备注
         */
        @JSONField(name = "trackRemark")
        private String trackRemark;

        /**
         * 签收日期
         */
        @JSONField(name = "deliveryDate")
        private String deliveryDate;

        /**
         * 订单费用
         */
        @JSONField(name = "orderFee")
        private BigDecimal orderFee;

        /**
         * 截单状态：0=未截单 1=已截单 2=自动拦截 3=仓内拦截
         */
        @JSONField(name = "interceptStatus")
        private Integer interceptStatus;

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

        /**
         * 费用明细（queryPage 接口特有，search 接口不返回此字段）
         */
        @JSONField(name = "statementList")
        private List<StatementDTO> statementList;
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
         * 物流跟踪号（即 ERP trackNo，出库后由 WEGO 回填）
         */
        @JSONField(name = "trackingNum")
        private String trackingNum;

        /**
         * 包裹尺寸（具体单位以 WEGO 实际返回为准）
         */
        @JSONField(name = "size")
        private Integer size;

        /**
         * 重量
         */
        @JSONField(name = "weight")
        private Integer weight;
    }

    /**
     * 费用明细（queryPage 接口特有）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatementDTO implements Serializable {

        /**
         * 订单号
         */
        @JSONField(name = "documentNo")
        private String documentNo;

        /**
         * 费用名称
         */
        @JSONField(name = "name")
        private String name;

        /**
         * 日期（YYYY-MM-DD）
         */
        @JSONField(name = "date")
        private String date;

        /**
         * 金额（2位小数 0.00 格式）
         */
        @JSONField(name = "fee")
        private BigDecimal fee;

        /**
         * 是否含税：0=不含税 1=含税
         */
        @JSONField(name = "taxFlag")
        private Integer taxFlag;

        /**
         * 费用备注
         */
        @JSONField(name = "remark")
        private String remark;
    }
}
