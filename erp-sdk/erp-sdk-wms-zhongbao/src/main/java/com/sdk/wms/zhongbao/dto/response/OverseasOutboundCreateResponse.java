package com.sdk.wms.zhongbao.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/10 11:00
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundCreateResponse {

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
        @JSONField(name = "orderNo")
        private String orderNo;

        /**
         * 物流跟踪号
         */
        @JSONField(name = "trackingNo")
        private String trackingNo;
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
