package com.erp.model.sys.entity;

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


/**
 * <p>
 * 三方通知推送记录
 * </p>
 *
 * @author jack
 * @since 2025-05-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "third_notice_push_record", autoResultMap = true)
public class ThirdNoticePushRecordEntity extends BaseEntity<ThirdNoticePushRecordEntity> {

    /**
    * cfg_third_notice_id
    */
    @TableField("cfg_third_notice_id")
    private String cfgThirdNoticeId;
    /**
    * 通知类型：messagePush=消息通知,approvalPush=审批推送  枚举：ThirdNoticePushRecordNoticeTypeEnum
    */
    @TableField("notice_type")
    private String noticeType;
    /**
    * 业务主表id
    */
    @TableField("business_id")
    private String businessId;
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
    * 通知节点
    */
    @TableField("notice_node")
    private String noticeNode;
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
    * 通知内容
    */
    @TableField("content")
    private String content;
    /**
    * 状态：success=推送成功, failed=推送失败  枚举：ThirdNoticePushRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 失败原因
    */
    @TableField("error_reason")
    private String errorReason;

    /**
     * json数据
     */
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dataJson;

    public static final String CFG_THIRD_NOTICE_ID = "cfg_third_notice_id";

    public static final String NOTICE_TYPE = "notice_type";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String BUSINESS_CODE = "business_code";

    public static final String NOTICE_NODE = "notice_node";

    public static final String NOTICE_METHOD = "notice_method";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String SEND_TIME = "send_time";

    public static final String TITLE = "title";

    public static final String STATUS = "status";

    public static final String ERROR_REASON = "error_reason";

    public static final String DATA_JSON = "data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
