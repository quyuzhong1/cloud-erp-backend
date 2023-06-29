package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 中台同步任务表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_sync_task")
public class DmpSyncTaskEntity extends BaseEntity<DmpSyncTaskEntity> {


    /**
    * 目标平台名称
    */
    @TableField("target_platform_name")
    private String targetPlatformName;

    /**
    * MQ消息主题
    */
    @TableField("mq_topic")
    private String mqTopic;

    /**
    * MQ消息TAG
    */
    @TableField("mq_tag")
    private String mqTag;

    /**
    * MQ消息内容
    */
    @TableField("mq_data")
    private String mqData;

    /**
    * 同步状态-1同步失败，0未同步，1同步成功
    */
    @TableField("status")
    private String status;

    /**
    * 同步返回信息
    */
    @TableField("return_msg")
    private String returnMsg;

    /**
    * 最新同步时间
    */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    /**
    * 来源系统
    */
    @TableField("source_platform_name")
    private String sourcePlatformName;

    /**
    * 来源单据类型
    */
    @TableField("souce_type")
    private String souceType;

    /**
    * 来源单据id
    */
    @TableField("source_id")
    private String sourceId;


    public static final String TARGET_PLATFORM_NAME = "target_platform_name";

    public static final String MQ_TOPIC = "mq_topic";

    public static final String MQ_TAG = "mq_tag";

    public static final String MQ_DATA = "mq_data";

    public static final String STATUS = "status";

    public static final String RETURN_MSG = "return_msg";

    public static final String LAST_SYNC_TIME = "last_sync_time";

    public static final String SOURCE_PLATFORM_NAME = "source_platform_name";

    public static final String SOUCE_TYPE = "souce_type";

    public static final String SOURCE_ID = "source_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}