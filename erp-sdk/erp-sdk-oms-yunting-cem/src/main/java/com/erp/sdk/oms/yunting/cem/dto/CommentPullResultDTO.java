package com.erp.sdk.oms.yunting.cem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评论拉取结果DTO
 *
 * @author ERP System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPullResultDTO {

    /**
     * 拉取批次
     */
    private Integer batchIndex;

    /**
     * 本批次数据量
     */
    private Integer count;

    /**
     * 评论数据列表
     */
    private List<YuntingCommentDTO> comments;

    /**
     * 下一页Token
     */
    private String pageToken;

    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 拉取时间戳
     */
    private Long pullTimestamp;
}

