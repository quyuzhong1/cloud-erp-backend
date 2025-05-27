package com.erp.model.workflow.entity;

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
 * 
 * </p>
 *
 * @author will
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_process_management")
public class ThirdProcessManagementEntity extends BaseEntity<ThirdProcessManagementEntity> {

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
    * 系统用户id
    */
    @TableField("sys_user_id")
    private String sysUserId;
    /**
    * 三方用户id
    */
    @TableField("third_user_id")
    private String thirdUserId;
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
    * 单据类型
    */
    @TableField("business_key")
    private String businessKey;
    /**
    * 流程状态
    */
    @TableField("status")
    private String status;
    /**
    * 	
审批名称
    */
    @TableField("process_instance_name")
    private String processInstanceName;
    /**
    * 来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;


    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String SYS_USER_ID = "sys_user_id";

    public static final String THIRD_USER_ID = "third_user_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_KEY = "business_key";

    public static final String STATUS = "status";

    public static final String PROCESS_INSTANCE_NAME = "process_instance_name";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}