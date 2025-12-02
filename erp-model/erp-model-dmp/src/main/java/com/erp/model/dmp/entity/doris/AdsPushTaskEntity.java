package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * ads推送任务
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("ads_push_task")
public class AdsPushTaskEntity extends BaseEntity<AdsPushTaskEntity> {

    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 流程 ID
    */
    @TableField("flow_id")
    private String flowId;
    /**
    * 节点 ID
    */
    @TableField("node_id")
    private String nodeId;
    /**
    * 实例ID/任务ID
    */
    @TableField("instance_id")
    private String instanceId;
    /**
    * 唯一编码
    */
    @TableField("unique_code")
    private String uniqueCode;
    /**
    * 数据MD5
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 来源平台
    */
    @TableField("source_platform_name")
    private String sourcePlatformName;
    /**
    * 目标平台
    */
    @TableField("target_platform_name")
    private String targetPlatformName;
    /**
    * 业务主题
    */
    @TableField("bill_topic")
    private String billTopic;
    /**
    * 单据主键
    */
    @TableField("bill_key")
    private String billKey;
    /**
    * 上游单据业务主题
    */
    @TableField("parent_bill_topic")
    private String parentBillTopic;
    /**
    * 上游单据主键(多个英文,拼接)
    */
    @TableField("parent_bill_key")
    private String parentBillKey;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 推送状态：init=待推送,finish=推送成功,error=推送失败
    */
    @TableField("status")
    private String status;
    /**
    * 请求报文
    */
    @TableField("request_data")
    private String requestData;
    /**
    * 响应报文
    */
    @TableField("response_data")
    private String responseData;
    /**
    * 错误次数
    */
    @TableField("error_count")
    private Integer errorCount;
    /**
    * 无需同步状态：push=需要推送，black=黑名单无需推送，self=工人无需推送
    */
    @TableField("push_status")
    private String pushStatus;
    /**
    * 推送消费流程url
    */
    @TableField("push_flow_url")
    private String pushFlowUrl;


    public static final String TASK_ID = "task_id";

    public static final String FLOW_ID = "flow_id";

    public static final String NODE_ID = "node_id";

    public static final String INSTANCE_ID = "instance_id";

    public static final String UNIQUE_CODE = "unique_code";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_PLATFORM_NAME = "source_platform_name";

    public static final String TARGET_PLATFORM_NAME = "target_platform_name";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String BILL_KEY = "bill_key";

    public static final String PARENT_BILL_TOPIC = "parent_bill_topic";

    public static final String PARENT_BILL_KEY = "parent_bill_key";

    public static final String SOURCE_ID = "source_id";

    public static final String STATUS = "status";

    public static final String REQUEST_DATA = "request_data";

    public static final String RESPONSE_DATA = "response_data";

    public static final String ERROR_COUNT = "error_count";

    public static final String PUSH_STATUS = "push_status";

    public static final String PUSH_FLOW_URL = "push_flow_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}