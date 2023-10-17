package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


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
@TableName("dmp_pull_task")
@NoArgsConstructor
@AllArgsConstructor
public class DmpPullTaskEntity extends BaseEntity<DmpPullTaskEntity> {


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
    * 同步状态SyncKingdeeStatusEnum
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
    @TableField("source_type")
    private String sourceType;

    /**
    * 来源单据id
    */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单据编号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 重试次数
     */
    @TableField("retry_times")
    private Integer retryTimes;


    public static final String TARGET_PLATFORM_NAME = "target_platform_name";

    public static final String MQ_TOPIC = "mq_topic";

    public static final String MQ_TAG = "mq_tag";

    public static final String MQ_DATA = "mq_data";

    public static final String STATUS = "status";

    public static final String RETURN_MSG = "return_msg";

    public static final String LAST_SYNC_TIME = "last_sync_time";

    public static final String SOURCE_PLATFORM_NAME = "source_platform_name";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public DmpPullTaskEntity(String targetPlatformName, String mqTopic, String mqTag, String mqData, String status,
                             String sourcePlatformName, String sourceType, String sourceId, String sourceCode, int retryTimes) {
    this.targetPlatformName = targetPlatformName;
    this.mqTopic = mqTopic;
    this.mqTag = mqTag;
    this.mqData = mqData;
    this.status = status;
    this.sourcePlatformName = sourcePlatformName;
    this.sourceType = sourceType;
    this.sourceId = sourceId;
    this.sourceCode = sourceCode;
    this.retryTimes = retryTimes;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}