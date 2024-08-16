package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SyncStatusEnum;
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
 * @author Cloud
 * @since 2023-09-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_push_task")
@AllArgsConstructor
@NoArgsConstructor
public class DmpPushTaskEntity extends BaseEntity<DmpPushTaskEntity> {

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

    /**
     * 第三方单号
     */
    @TableField("third_code")
    private String thirdCode;


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

    public static final String RETRY_TIMES = "retry_times";

    public static final String THIRD_CODE = "third_code";

   

    public DmpPushTaskEntity(DmpPushTaskFeignDTO dto) {
        this.targetPlatformName = dto.getTargetPlatformName();
        this.mqTopic = dto.getMqTopic();
        this.mqTag = dto.getMqTag();
        this.mqData = dto.getMqData();
        this.status = SyncStatusEnum.IN_SYNC.getCode();
        this.sourcePlatformName = dto.getSourcePlatformName();
        this.sourceType = dto.getSourceType();
        this.sourceId = dto.getSourceId();
        this.sourceCode = dto.getSourceCode();
        this.syncOperate = dto.getSyncOperate();
        this.parentId = dto.getParentId();
        this.thirdCode = dto.getThirdCode();
        this.retryTimes = 0;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}