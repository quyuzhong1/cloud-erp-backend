package com.erp.model.msg.dto;

import lombok.Data;

/**
 * 通知消息卡片详情信息
 * @Auther will
 * @Date 2025/2/13 10:12
 */
@Data
public class NoticeMsgCardButtonDTO {

    /**
     * 按钮名称
     */
    private String name;
    /**
     * 按钮url
     */
    private String url;
}
