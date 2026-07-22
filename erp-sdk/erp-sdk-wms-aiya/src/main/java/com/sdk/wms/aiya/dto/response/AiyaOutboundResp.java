package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AIYA 2C 出库单查询响应结构（{@code GLINK_QUERY_ORDER_NOTIFY}）。
 * <p>
 * 《爱亚海外仓对接方案文档》6.3.3「4、爱亚出库单查询」描述为扁平结构（不同于 WEGO 的
 * {@code result:{list,pages,emptyFlag}} 嵌套分页结构），本类去掉了骨架时期照抄 WEGO 的
 * {@code PageResultDTO}/{@code logisticsList} 嵌套设计。顶层列表字段名 {@code resultList}
 * 参照同族接口 {@code GLINK_QUERY_WAREHOUSE_NOTIFY}/{@code GLINK_QUERY_CARRIER_NOTIFY}
 * （均为 {@code GLINK_QUERY_XXX_NOTIFY} 命名、均用 {@code resultList}）类推得出，<b>未有真实
 * 出库单查询响应样例验证</b>，字段名/是否分页均为推测，待联调后按真实样例修正，详见
 * docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
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
     * 状态码（成功时通常为 SUCCESS，与其它 AIYA 接口保持一致的小写 {@code code} 字段名）
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 提示信息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 2C 出库单列表（推测字段名，未有真实样例验证）
     */
    @JSONField(name = "resultList")
    private List<OutboundOrderDTO> resultList;

    /**
     * 2C 出库单（推测字段清单，未有真实样例验证）。
     * <p>
     * 与建单请求 {@link com.erp.model.wms.dto.AiyaOutboundSaveDTO} 对齐：AIYA 不回传独立出库单号，
     * {@code orderNumber} 即建单时下发的幂等键，查询/取消均以此号为 key。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OutboundOrderDTO implements Serializable {

        /**
         * 客户交易物流订单号（建单时下发的幂等键，AIYA 不回传独立出库单号）
         */
        @JSONField(name = "orderNumber")
        private String orderNumber;

        /**
         * 客户销售平台编号（建单时下发的 extOrderNumber，可选）
         */
        @JSONField(name = "extOrderNumber")
        private String extOrderNumber;

        /**
         * 仓库编码
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 订单状态字母码：A-已出库/B-已取消/C-库存不足/D-锁住，见 {@link com.sdk.wms.aiya.enums.AiyaEnums.OrderStatusEnum}
         */
        @JSONField(name = "orderStatus")
        private String orderStatus;

        /**
         * 建单时下发的订单时间（{@code yyyy-MM-dd'T'HH:mm:ssZ}）
         */
        @JSONField(name = "orderTime")
        private String orderTime;

        /**
         * 出库完成时间（推测字段名，未有真实样例验证；用于下游 dateShipping 映射）
         */
        @JSONField(name = "finishTime")
        private String finishTime;

        /**
         * 承运商（建单时下发的 shippingInstructions.carrier）
         */
        @JSONField(name = "carrier")
        private String carrier;

        /**
         * 承运商服务等级（建单时下发的 shippingInstructions.carrierService）
         */
        @JSONField(name = "carrierService")
        private String carrierService;

        /**
         * 物流跟踪号（建单 ATTACHMENT 模式下发的 trackingNumber，或 API 模式由 AIYA 回填）
         */
        @JSONField(name = "trackingNumber")
        private String trackingNumber;

        /**
         * 产品明细
         */
        @JSONField(name = "items")
        private List<ItemDTO> items;

        /**
         * 订单备注
         */
        @JSONField(name = "remark")
        private String remark;

        /**
         * 错误信息（提交失败/库存不足/出库异常时回填）
         */
        @JSONField(name = "errorMessage")
        private String errorMessage;
    }

    /**
     * 产品明细（单 SKU 行，字段与建单请求 {@code items[]} 对齐：{@code sku}/{@code quantity}）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDTO implements Serializable {

        /**
         * 海外仓平台 SKU 编码
         */
        @JSONField(name = "sku")
        private String sku;

        /**
         * 发货数量
         */
        @JSONField(name = "quantity")
        private Integer quantity;
    }
}
