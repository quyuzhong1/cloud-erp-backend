package com.erp.server.msg.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-01  10:00
 * @Author: zhangchunlin
 */
@Data
public class WarnMsgContentDTO implements Serializable {

    /**
     * 预警标题
     */
    private String title;

    /**
     * 预警内容
     */
    private String content;

}