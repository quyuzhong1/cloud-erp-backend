package com.sdk.wms.jitu.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 极兔出库单创建响应
 */
@Data
public class StockOutOrderCreateResponse extends BaseResponse {

    /**
     * 返回列表
     */
    private List<ResponseItem> responseitems;

    @Data
    public static class ResponseItem {
        /**
         * 客户订单号
         */
        private String txlogisticid;

        /**
         * 运单号
         */
        private String mailno;

        /**
         * 出库单号
         */
        private String deliveryOrderCode;

        /**
         * 是否成功
         */
        private String success;

        /**
         * 错误编码
         */
        private String reason;

        /**
         * 错误信息
         */
        private String message;
    }
}