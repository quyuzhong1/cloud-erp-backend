package com.erp.sdk.oms.yunting.cem.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论情感枚举
 *
 * @author ERP System
 */
@Getter
@AllArgsConstructor
public enum CommentSentimentEnum {

    /**
     * 正面
     */
    POSITIVE("正面", "Positive"),

    /**
     * 中性
     */
    NEUTRAL("中性", "Neutral"),

    /**
     * 负面
     */
    NEGATIVE("负面", "Negative"),

    /**
     * 混合
     */
    MIXED("混合", "Mixed"),

    /**
     * 无情感
     */
    NO_SENTIMENT("无情感", "NoSentiment");

    /**
     * 中文名称
     */
    private final String chineseName;

    /**
     * 英文名称
     */
    private final String englishName;

    /**
     * 根据中文名称获取枚举
     */
    public static CommentSentimentEnum getByChineseName(String chineseName) {
        for (CommentSentimentEnum sentiment : values()) {
            if (sentiment.getChineseName().equals(chineseName)) {
                return sentiment;
            }
        }
        return null;
    }
}

