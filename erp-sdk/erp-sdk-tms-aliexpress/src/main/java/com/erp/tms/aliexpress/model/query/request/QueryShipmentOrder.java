package com.erp.tms.aliexpress.model.query.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryShipmentOrder {

    /**
     * 交易单号
     */
    private String trade_order_id;

    /**
     * 子单列表
     */
    private List<SubTradeOrder> sub_trade_order_list;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubTradeOrder {

        /**
         * 申明发货类型 part / all
         * (必须)
         */
        private String send_type;

        /**
         * 发货列表
         * (必须)
         */
        private List<Shipment> shipment_list;

        /**
         * 子交易单序号
         * (必须)
         */
        private String sub_trade_order_index;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Shipment {

        /**
         * 实际承运商--物流方案为“卖家自定义-中国”(OTHER)时该字段为必填
         * （非必须）
         */
        private String actual_carrier;

        /**
         * 运单号轨迹查询地址-物流方案为自定义(OTHER开头)的时候必填
         * （非必须）
         */
        private String tracking_web_site;

        /**
         * 货物跟踪编码/国际运单号
         * (必须)
         */
        private String logistics_no;

        /**
         * 物流方案编码
         * (必须)
         */
        private String service_name;


    }
}
