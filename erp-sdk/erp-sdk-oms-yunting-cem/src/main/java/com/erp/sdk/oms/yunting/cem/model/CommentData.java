package com.erp.sdk.oms.yunting.cem.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 社交媒体评论数据
 *
 * @author ERP System
 */
@Data
public class CommentData {

    /**
     * 唯一键，用于去重和覆盖
     */
    @SerializedName("unique")
    private String unique;

    /**
     * 评论发布时间，格式：yyyy-MM-dd HH:mm:ss
     */
    @SerializedName("publishTime")
    private String publishTime;

    /**
     * 来源平台，如：小红书、微博、抖音等
     */
    @SerializedName("sourceName")
    private String sourceName;

    /**
     * 链接任务名称
     */
    @SerializedName("connectionName")
    private String connectionName;

    /**
     * 项目名称
     */
    @SerializedName("projectName")
    private String projectName;

    /**
     * 原平台链接
     */
    @SerializedName("url")
    private String url;

    /**
     * 帖子标题
     */
    @SerializedName("title")
    private String title;

    /**
     * 主要内容（文本）
     */
    @SerializedName("content")
    private String content;

    /**
     * 用户ID
     */
    @SerializedName("userId")
    private String userId;

    /**
     * 用户昵称
     */
    @SerializedName("userName")
    private String userName;

    /**
     * 用户头像
     */
    @SerializedName("userImg")
    private String userImg;

    /**
     * 原文/帖子/评论/回复的分组ID
     */
    @SerializedName("group")
    private String group;

    /**
     * 父级ID
     */
    @SerializedName("parent")
    private String parent;

    /**
     * 原始ID
     */
    @SerializedName("oid")
    private String oid;

    /**
     * 数据类型，如：帖子/评论/回复
     */
    @SerializedName("dataLevel")
    private String dataLevel;

    /**
     * 是否无效内容：是/否
     */
    @SerializedName("isDefault")
    private String isDefault;

    /**
     * 消息情感：正面/中性/负面/混合
     */
    @SerializedName("escore")
    private String escore;

    /**
     * 图片URL列表
     */
    @SerializedName("pictures")
    private List<String> pictures;

    /**
     * 视频URL列表
     */
    @SerializedName("videos")
    private List<String> videos;

    /**
     * 评论数
     */
    @SerializedName("comments")
    private Integer comments;

    /**
     * 点赞数
     */
    @SerializedName("likes")
    private Integer likes;

    /**
     * 浏览数
     */
    @SerializedName("views")
    private Integer views;

    /**
     * 存储时间，毫秒时间戳
     */
    @SerializedName("insertTimestamp")
    private Long insertTimestamp;

    /**
     * 主题配置列表
     */
    @SerializedName("topicConfigs")
    private List<TopicConfig> topicConfigs;

    /**
     * 标签列表
     */
    @SerializedName("tagList")
    private List<Tag> tagList;

    /**
     * 热词列表
     */
    @SerializedName("textRanks")
    private List<String> textRanks;

    /**
     * 热门短语列表
     */
    @SerializedName("phrases")
    private List<String> phrases;

    /**
     * 翻译结果列表
     */
    @SerializedName("translateList")
    private List<TranslateInfo> translateList;

    /**
     * 主题配置
     */
    @Data
    public static class TopicConfig {
        /**
         * 主题名称
         */
        @SerializedName("topicName")
        private String topicName;

        /**
         * 主题值（可能有多个）
         */
        @SerializedName("topicValue")
        private List<String> topicValue;
    }

    /**
     * 标签
     */
    @Data
    public static class Tag {
        /**
         * 标签情感：正面/中性/负面/无情感
         */
        @SerializedName("escore")
        private String escore;

        /**
         * 末级标签名称
         */
        @SerializedName("tagName")
        private String tagName;
    }

    /**
     * 翻译信息
     */
    @Data
    public static class TranslateInfo {
        /**
         * 翻译类型：CONTENT=内容，COMMENT_TITLE=标题
         */
        @SerializedName("translateField")
        private String translateField;

        /**
         * 翻译结果
         */
        @SerializedName("translateResult")
        private String translateResult;

        /**
         * 原始内容语种
         */
        @SerializedName("originLanguage")
        private String originLanguage;

        /**
         * 翻译结果语种
         */
        @SerializedName("targetLanguage")
        private String targetLanguage;
    }
}

