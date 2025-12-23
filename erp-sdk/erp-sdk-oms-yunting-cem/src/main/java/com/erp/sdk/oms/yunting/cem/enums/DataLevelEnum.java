package com.erp.sdk.oms.yunting.cem.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型枚举
 *
 * @author ERP System
 */
@Getter
@AllArgsConstructor
public enum DataLevelEnum {

    /**
     * 帖子
     */
    POST("帖子", "Post"),

    /**
     * 评论
     */
    COMMENT("评论", "Comment"),

    /**
     * 回复
     */
    REPLY("回复", "Reply");

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
    public static DataLevelEnum getByChineseName(String chineseName) {
        for (DataLevelEnum level : values()) {
            if (level.getChineseName().equals(chineseName)) {
                return level;
            }
        }
        return null;
    }
}

