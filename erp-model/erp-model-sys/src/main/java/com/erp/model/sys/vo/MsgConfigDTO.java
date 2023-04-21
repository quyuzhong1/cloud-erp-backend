package com.erp.model.sys.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: MsgConfigDTO
 * @Description: TODO
 * @CreateTime: 2023-04-21  14:17
 * @Author: zhangchunlin
 */
@Data
public class MsgConfigDTO implements Serializable {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 发送标识
     */
    private Boolean sendFlag;

    /**
     * 最大重试次数
     */
    private Integer retryTimes;

    /**
     * 扩展字段1
     */
    private String extend1;


}