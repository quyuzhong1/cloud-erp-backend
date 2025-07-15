package com.erp.model.workflow.entity;

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
 * 三方流程实例清单
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_process_instance")
public class ThirdProcessInstanceEntity extends BaseEntity<ThirdProcessInstanceEntity> {

    /**
    * 审批名称
    */
    @TableField("approval_name")
    private String approvalName;
    /**
    * 审批创建时间
    */
    @TableField("start_time")
    private String startTime;
    /**
    * 审批完成时间
    */
    @TableField("end_time")
    private String endTime;
    /**
    * 审批单编号
    */
    @TableField("serial_number")
    private String serialNumber;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 审批表单控件 JSON 字符串
    */
    @TableField("form")
    private String form;
    /**
    * 审批定义 Code
    */
    @TableField("approval_code")
    private String approvalCode;
    /**
    * 审批实例 Code
    */
    @TableField("instance_code")
    private String instanceCode;
    /**
     * 是否转换完成
     */
    @TableField("is_complete")
    private Boolean isComplete;
    /**
     * 审批任务集合
     */
    @TableField("task_list")
    private String taskList;


    public static final String APPROVAL_NAME = "approval_name";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String SERIAL_NUMBER = "serial_number";

    public static final String STATUS = "status";

    public static final String FORM = "form";

    public static final String APPROVAL_CODE = "approval_code";

    public static final String INSTANCE_CODE = "instance_code";

    public static final String IS_COMPLETE = "is_complete";

    public static final String TASK_LIST = "task_list";

    @Override
    public Serializable pkVal() {
        return null;
    }

}