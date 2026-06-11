package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * WEGO 2C 出库单创建/修改（interfaceType=2c.order.save）请求参数。
 *
 * <p>WEGO 同一接口承担「创建」与「修改」语义：</p>
 * <ul>
 *     <li>{@code no} 为空时执行新增，成功后响应 {@code result} 字段即为 WEGO 出库单号；</li>
 *     <li>{@code no} 非空时执行修改，需传入 WEGO 已有出库单号。</li>
 * </ul>
 *
 * <p>响应中 {@code result} 为字符串：成功时为 WEGO 订单号，失败时为详细错误原因。</p>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoOutboundSaveDTO implements Serializable {

    /**
     * 2C 出库单创建/修改请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveReqDTO implements Serializable {

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
         * 海外仓供应商名称（取自 overseas_provider.name，如 "WEGO"）
         */
        @NotBlank(message = "warehouseBusiness不能为空")
        private String warehouseBusiness;

        /**
         * 仓库代码（海外仓平台仓库代码，取自 overseas_provider_warehouse.platform_warehouse_code）
         */
        @NotBlank(message = "warehouseCode不能为空")
        private String warehouseCode;

        /**
         * WEGO 出库单号：为空时创建新单；非空时修改对应单据
         * （存于 third_warehouse_delivery.shipping_order_no）
         */
        private String no;

        /**
         * 店铺名，可选
         */
        private String shopName;

        /**
         * 收货人姓名
         */
        @NotBlank(message = "receiver不能为空")
        private String receiver;

        /**
         * 收货人电话
         */
        @NotBlank(message = "receiverPhone不能为空")
        private String receiverPhone;

        /**
         * 收货人邮编（最长 8 位）
         */
        @NotBlank(message = "receiverPostCode不能为空")
        @Size(max = 8, message = "receiverPostCode最长8位")
        private String receiverPostCode;

        /**
         * 收货人邮箱，可选
         */
        private String receiverEmail;

        /**
         * 收货人省/州
         */
        @NotBlank(message = "receiverProvince不能为空")
        private String receiverProvince;

        /**
         * 收货人城市
         */
        @NotBlank(message = "receiverCity不能为空")
        private String receiverCity;

        /**
         * 收货人区，可选
         */
        private String receiverArea;

        /**
         * 收货人地址
         */
        @NotBlank(message = "receiverAddress不能为空")
        private String receiverAddress;

        /**
         * ERP 参照编号（三方仓发货单号 WFHD...），WEGO 用于系统内查询订单回传时关联 ERP 单据
         */
        @NotBlank(message = "referenceCode不能为空")
        private String referenceCode;

        /**
         * 参照编号 2，可选（备用关联字段）
         */
        private String referenceCode2;

        /**
         * 产品明细，至少一条
         */
        @NotEmpty(message = "products不能为空")
        @Valid
        private List<Product> products;

        /**
         * 订单备注，可选
         */
        private String remark;

        /**
         * 发货人，可选
         */
        private String sender;

        /**
         * 发货人电话，可选
         */
        private String senderPhone;

        /**
         * 发货人邮箱，可选
         */
        private String senderEmail;

        /**
         * 指定收货日期（格式：YYYY-MM-DD），可选
         */
        private String receiveDate;

        /**
         * 指定收货时间段，可选
         */
        private String receiveTime;

        /**
         * 需要派送：0=需要（默认）1=不需要
         */
        @NotNull(message = "needSendFlag不能为空")
        private Integer needSendFlag;

        /**
         * 需要包装：0=需要 1=不需要（默认）
         */
        @NotNull(message = "needPackFlag不能为空")
        private Integer needPackFlag;

        /**
         * 面单类型：0=海外仓面单（默认）1=平台指定面单
         */
        @NotNull(message = "wayBillType不能为空")
        private Integer wayBillType;

        /**
         * 平台指定面单信息（wayBillType=1 时填写），可选
         */
        private WayBillUrl wayBillUrl;

        /**
         * 面单 base64（平台指定面单时与 wayBillUrl.files 二选一，推荐用 url），可选
         */
        private String wayBillBase64;

        /**
         * 指定派送渠道名称（必须为 WEGO transport.get 可查询的渠道名），可选
         */
        private String logisticsName;

        /**
         * 运单备注，可选
         */
        private String trackRemark;
    }

    /**
     * 产品明细（单 SKU 行）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Product implements Serializable {

        /**
         * 海外仓平台 SKU 编码（取自 sku_mapping 中的 platform_sku_no）
         */
        @NotBlank(message = "sku不能为空")
        private String sku;

        /**
         * 发货数量
         */
        @NotNull(message = "qty不能为空")
        @Min(value = 1, message = "qty最小为1")
        private Integer qty;
    }

    /**
     * 平台指定面单信息（wayBillType=1 时使用）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WayBillUrl implements Serializable {

        /**
         * 指定物流公司名称
         */
        private String logisticsName;

        /**
         * 指定物流单号（跟踪号）
         */
        private String trackingNum;

        /**
         * 指定面单 URL 数组（推荐方式）
         */
        private List<WayBillFile> files;
    }

    /**
     * 指定面单文件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WayBillFile implements Serializable {

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 面单 URL（需为可直接打印的格式）
         */
        private String fileUrl;
    }
}
