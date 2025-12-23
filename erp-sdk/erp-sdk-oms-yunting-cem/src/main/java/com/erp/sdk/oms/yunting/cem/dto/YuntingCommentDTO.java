package com.erp.sdk.oms.yunting.cem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 云听CEM社交媒体评论DTO
 *
 * @author ERP System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuntingCommentDTO {

    /**
     * 唯一键
     */
    private String unique;

    /**
     * 评论发布时间
     */
    private String publishTime;

    /**
     * 来源平台
     */
    private String sourceName;

    /**
     * 链接任务名称
     */
    private String connectionName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 原平台链接
     */
    private String url;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userImg;

    /**
     * 分组ID
     */
    private String group;

    /**
     * 父级ID
     */
    private String parent;

    /**
     * 原始ID
     */
    private String oid;

    /**
     * 数据类型
     */
    private String dataLevel;

    /**
     * 是否无效内容
     */
    private String isDefault;

    /**
     * 情感
     */
    private String escore;

    /**
     * 图片列表
     */
    private List<String> pictures;

    /**
     * 视频列表
     */
    private List<String> videos;

    /**
     * 评论数
     */
    private Integer comments;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 浏览数
     */
    private Integer views;

    /**
     * 存储时间戳
     */
    private Long insertTimestamp;

    /**
     * 主题配置
     */
    private String topicConfigsJson;

    /**
     * 标签列表
     */
    private String tagListJson;

    /**
     * 热词
     */
    private String textRanksJson;

    /**
     * 热门短语
     */
    private String phrasesJson;

    /**
     * 翻译列表
     */
    private String translateListJson;
}

