package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * <p>
 * 中台同步任务表
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
*/
@Getter
@Setter
@TableName("dmp_push_task_history")
public class DmpPushTaskHistoryEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除字段
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;

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
    * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
    */
    @TableField("status")
    private String status;
    /**
    * 同步返回信息（错误信息和成功信息）
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
    /**
    * 同步操作，同步其他系统的事件
    */
    @TableField("sync_operate")
    private String syncOperate;

    /**
     * 来源上级单据id
     */
    @TableField("parent_id")
    private String parentId;


}