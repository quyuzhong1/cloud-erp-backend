package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Classname: MsgConfig
 * @Description: 消息渠道配置
 * @CreateTime: 2023-04-21  11:52
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("msg_channel_config")
public class MsgChannelConfig extends BaseEntity<MsgChannelConfig> {

    /**
     * 消息配置key
     */
    @TableField("msg_config_id")
    private String msgConfigId;

    /**
     * 渠道
     */
    @TableField("channel_code")
    private String channelCode;

    /**
     * 渠道应用编码
     */
    @TableField("channel_app_code")
    private String channelAppCode;

    /**
     * 发送标识
     */
    @TableField("send_flag")
    private Boolean sendFlag;


    /**
     * 扩展字段1
     */
    @TableField("extend1")
    private String extend1;


    @Override
    public Serializable pkVal() {
        return null;
    }

}