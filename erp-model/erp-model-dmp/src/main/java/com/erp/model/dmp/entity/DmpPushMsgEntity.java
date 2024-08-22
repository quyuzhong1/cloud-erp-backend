package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 本地消息表
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_push_msg")
public class DmpPushMsgEntity extends BaseEntity<DmpPushMsgEntity> {

    /**
    * 本地消息id
    */
    @TableField("message_id")
    private String messageId;
    /**
    * 目标系统
    */
    @TableField("target_platform")
    private String targetPlatform;
    /**
    * 来源系统
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 操作类型
    */
    @TableField("sync_operate")
    private String syncOperate;
    /**
    * 推送数据
    */
    @TableField("push_data")
    private String pushData;
    /**
    * 消息创建时间
    */
    @TableField("message_create_time")
    private LocalDateTime messageCreateTime;
    /**
    * 消息更新时间
    */
    @TableField("message_update_time")
    private LocalDateTime messageUpdateTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
     * 父id
     */
    @TableField("parent_id")
    private String parentId;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String MESSAGE_ID = "message_id";

    public static final String TARGET_PLATFORM = "target_platform";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SYNC_OPERATE = "sync_operate";

    public static final String PUSH_DATA = "push_data";

    public static final String MESSAGE_CREATE_TIME = "message_create_time";

    public static final String MESSAGE_UPDATE_TIME = "message_update_time";

    public static final String REMARK = "remark";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}