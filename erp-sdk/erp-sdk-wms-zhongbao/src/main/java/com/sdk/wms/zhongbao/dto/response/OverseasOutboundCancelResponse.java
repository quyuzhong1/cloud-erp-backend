package com.sdk.wms.zhongbao.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/11 8:59
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundCancelResponse {

    /**
     * 状态码
     */
    @JSONField(name = "referenceNo")
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
         * 总数量
         */
        @JSONField(name = "totalQty")
        private Integer totalQty;

        /**
         * 物流跟踪号
         */
        @JSONField(name = "successList")
        private List<SuccessDTO> successList;

        /**
         * 物流跟踪号
         */
        @JSONField(name = "failList")
        private List<FailDTO> failList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SuccessDTO{
        /**
         * 成功单号
         */
        @JSONField(name = "成功单号")
        private String orderNo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FailDTO{
        /**
         * 失败单号
         */
        @JSONField(name = "orderNo")
        private String orderNo;
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
