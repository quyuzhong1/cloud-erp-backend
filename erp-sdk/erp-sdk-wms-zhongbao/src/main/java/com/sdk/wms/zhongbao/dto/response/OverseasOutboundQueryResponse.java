package com.sdk.wms.zhongbao.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/12 15:51
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
public class OverseasOutboundQueryResponse {

    public OverseasOutboundQueryResponse() {

    }

    /**
     * 状态码
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 成功与否
     */
    @JSONField(name = "success")
    private boolean success;

    /**
     * 返回数据
     */
    @JSONField(name = "data")
    private ResponseData responseData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseData {

        /**
         * 订单号
         */
        @JSONField(name = "totalCount")
        private String totalCount;

        /**
         * 物流跟踪号
         */
        @JSONField(name = "totalPage")
        private String totalPage;

        /**
         * 数据列表
         */
        @JSONField(name = "list")
        private List<DataList> list;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DataList {

        /**
         * 订单号
         */
        @JSONField(name = "orderNo")
        private String orderNo;

        /**
         * 订单号
         */
        @JSONField(name = "referenceNo")
        private String referenceNo;

        /**
         * 物流跟踪号
         */
        @JSONField(name = "trackingNo")
        private String trackingNo;

        /**
         * 状态
         * 5=>已出库：数大臣自动变更B2B三方发货单和订单已发货，并生成出库单
         * -1=>已取消：数大臣自动发起拦截，走拦截逻辑接口取消订单；如果订单已经是拦截中，则直接拦截成功，发货单变更为取消发货，订单变更为审核不通过-待配货
         * 1=>草稿,2=>待审核,3=>已审核,4=>待出库：数大臣单据不做状态变更
         * -2=>异常：数大臣单据不做状态变更，但是三方发货单需要增加操作日志记录详情：海外仓出库异常，系统应拦截，为保证发货时效运营要求不予拦截，直接海外仓后台修改提交，异常信息【errorReason】
         */
        @JSONField(name = "status")
        private Integer status;

        /**
         * 异常原因
         */
        @JSONField(name = "errorReason")
        private String errorReason;
    }

    /**
     * 提示消息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 错误消息列表
     */
    @JSONField(name = "errors")
    private List<String> errors;
}
