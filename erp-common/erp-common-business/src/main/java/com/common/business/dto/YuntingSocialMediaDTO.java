package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 云听社媒数据消费DTO
 * @author wuhaotian
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class YuntingSocialMediaDTO extends UniqueDto {

    /**
     * 来源ID
     */
    private String sourceId;

    /**
     * 父级
     */
    private String parent;

    /**
     * 视频
     */
    private String videos;

    /**
     * OID
     */
    private String oid;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String pictures;

    /**
     * 内容
     */
    private String content;

    /**
     * 评分
     */
    private String escore;

    /**
     * 浏览量
     */
    private String views;

    /**
     * 分组
     */
    private String group;

    /**
     * 点赞数
     */
    private String likes;

    /**
     * 评论数
     */
    private String comments;

    /**
     * URL
     */
    private String url;

    /**
     * 唯一标识
     */
    private String unique;

    /**
     * 短语
     */
    private String phrases;

    /**
     * 用户头像
     */
    private String userImg;

    /**
     * 翻译列表
     */
    private String translateList;

    /**
     * 入库时间戳
     */
    private String insertTimestamp;

    /**
     * 主题配置
     */
    private String topicConfigs;

    /**
     * 发布时间
     */
    private String publishTime;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 是否默认
     */
    private String isDefault;

    /**
     * 文本排名
     */
    private String textRanks;

    /**
     * 来源名称
     */
    private String sourceName;

    /**
     * 连接名称
     */
    private String connectionName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 数据级别
     */
    private String dataLevel;

    /**
     * 平台（固定为yunting）
     */
    private String platform;

    /**
     * 下载时间
     */
    private LocalDateTime downloadTime;
}

