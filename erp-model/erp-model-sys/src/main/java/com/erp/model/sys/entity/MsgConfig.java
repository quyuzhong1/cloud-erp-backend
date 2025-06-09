package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Classname: MsgConfig
 * @Description: 消息配置
 * @CreateTime: 2023-04-21  11:52
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("msg_config")
@EqualsAndHashCode
public class MsgConfig extends BaseEntity<MsgConfig> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 渠道
     */
    @TableField("channel")
    private String channel;

    /**
     * 消息类型
     */
    @TableField("msg_type")
    private String msgType;

    /**
     * 发送标识
     */
    @TableField("send_flag")
    private Boolean sendFlag;

    /**
     * 最大重试次数
     */
    @TableField("retry_times")
    private Integer retryTimes;

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