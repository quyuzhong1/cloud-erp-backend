package com.erp.model.workflow.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import org.apache.ibatis.type.JdbcType;


/**
 * <p>
 * ERP审批同步-通知配置
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "approve_sync_record", autoResultMap = true)
public class ApproveSyncRecordEntity extends BaseEntity<ApproveSyncRecordEntity> implements Serializable{

    /**
    * 主表id
    */
    @TableField("cfg_approve_sync_id")
    private String cfgApproveSyncId;
    /**
    * 通知类型：messagePush=消息通知,approvalPush=审批推送  枚举：ApproveSyncRecordNoticeTypeEnum
    */
    @TableField("notice_type")
    private String noticeType;
    /**
    * 单据名称
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 单据单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 提醒方式
    */
    @TableField("notice_method")
    private String noticeMethod;
    /**
    * 接收人id
    */
    @TableField("receiver_id")
    private String receiverId;
    /**
    * 接收人
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 发送时间
    */
    @TableField("send_time")
    private LocalDateTime sendTime;
    /**
    * 通知标题
    */
    @TableField("title")
    private String title;
    /**
    * 状态：success=推送成功, failed=推送失败  枚举：ApproveSyncRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 失败原因
    */
    @TableField("error_reason")
    private String errorReason;
    /**
     * 通知节点
     */
    @TableField("notice_node")
    private String noticeNode;

    /**
     * json数据
     */
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dataJson;

    /**
     * message_id
     */
    @TableField("message_id")
    private String messageId;

    public static final String CFG_APPROVE_SYNC_ID = "cfg_approve_sync_id";

    public static final String NOTICE_TYPE = "notice_type";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String BUSINESS_CODE = "business_code";

    public static final String NOTICE_METHOD = "notice_method";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String SEND_TIME = "send_time";

    public static final String TITLE = "title";

    public static final String STATUS = "status";

    public static final String ERROR_REASON = "error_reason";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
