package com.erp.sdk.oms.yunting.cem.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 社交媒体评论数据拉取响应
 *
 * @author ERP System
 */
@Data
public class CommentPullResponse {

    /**
     * 状态码，20000表示成功
     */
    @SerializedName("code")
    private Integer code;

    /**
     * 状态信息
     */
    @SerializedName("msg")
    private String msg;

    /**
     * 追踪ID，用于标识某一次具体的请求
     */
    @SerializedName("traceId")
    private String traceId;

    /**
     * 结果数据
     */
    @SerializedName("result")
    private CommentResult result;

    /**
     * 评论结果数据
     */
    @Data
    public static class CommentResult {

        /**
         * 当前批次返回的数据数量
         */
        @SerializedName("count")
        private Integer count;

        /**
         * 评论数据列表
         */
        @SerializedName("data")
        private List<CommentData> data;

        /**
         * 下次请求的位移参数
         */
        @SerializedName("pageToken")
        private String pageToken;

        /**
         * 是否还有更多数据
         */
        @SerializedName("hasMore")
        private Boolean hasMore;
    }
}

