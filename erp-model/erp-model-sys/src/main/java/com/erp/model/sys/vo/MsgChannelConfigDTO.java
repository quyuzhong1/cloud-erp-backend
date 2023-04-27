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
public class MsgChannelConfigDTO implements Serializable {

    /**
     * 消息配置key
     */
    private String msgConfigId;

    /**
     * 渠道
     */
    private String channelCode;

    /**
     * 渠道应用编码
     */
    private String channelAppCode;

    /**
     * 发送标识
     */
    private Boolean sendFlag;


    /**
     * 扩展字段1
     */
    private String extend1;



}