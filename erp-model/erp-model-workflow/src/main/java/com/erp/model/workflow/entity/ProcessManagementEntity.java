package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("process_management")
public class ProcessManagementEntity extends BaseEntity<ProcessManagementEntity> {

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * 业务ID
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 业务编码
     */
    @TableField("business_code")
    private String businessCode;

    /**
     * 当前节点ID
     */
    @TableField("current_node_id")
    private String currentNodeId;

    /**
     * 流程状态
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Date endTime;


    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String CURRENT_NODE_ID = "current_node_id";

    public static final String PROCESS_STATUS = "process_status";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
