package com.erp.sdk.oms.yunting.cem.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

/**
 * 社交媒体评论数据拉取请求
 *
 * @author ERP System
 */
@Data
@Builder
public class CommentPullRequest {

    /**
     * 项目ID（必填）
     */
    @SerializedName("projectId")
    private String projectId;

    /**
     * 开始时间（选填），格式：yyyy-MM-dd HH:mm:ss
     */
    @SerializedName("startTime")
    private String startTime;

    /**
     * 结束时间（选填），格式：yyyy-MM-dd HH:mm:ss
     */
    @SerializedName("endTime")
    private String endTime;

    /**
     * 分页位移参数（选填）
     * 首次请求不传，后续请求传上次响应返回的pageToken
     */
    @SerializedName("pageToken")
    private String pageToken;
}

