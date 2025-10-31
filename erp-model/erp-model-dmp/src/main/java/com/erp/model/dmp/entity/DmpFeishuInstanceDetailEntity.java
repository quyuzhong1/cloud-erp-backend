package com.erp.model.dmp.entity;

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
 * DMP飞书审批实例详情记录表
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_feishu_instance_detail")
public class DmpFeishuInstanceDetailEntity extends BaseEntity<DmpFeishuInstanceDetailEntity> {

    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 主键ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 审批名称
    */
    @TableField("approval_name")
    private String approvalName;
    /**
    * 开始时间
    */
    @TableField("start_time")
    private String startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private String endTime;
    /**
    * 用户ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 用户OpenID
    */
    @TableField("open_id")
    private String openId;
    /**
    * 审批流水号
    */
    @TableField("serial_number")
    private String serialNumber;
    /**
    * 部门ID
    */
    @TableField("department_id")
    private String departmentId;
    /**
    * 审批状态
    */
    @TableField("status")
    private String status;
    /**
    * 唯一标识UUID
    */
    @TableField("uuid")
    private String uuid;
    /**
    * 表单内容(JSON文本)
    */
    @TableField("form")
    private String form;
    /**
    * 任务列表(JSON文本)
    */
    @TableField("task_list")
    private String taskList;
    /**
    * 评论列表(JSON文本)
    */
    @TableField("comment_list")
    private String commentList;
    /**
    * 时间线(JSON文本)
    */
    @TableField("timeline")
    private String timeline;
    /**
    * 修改后实例编码
    */
    @TableField("modified_instance_code")
    private String modifiedInstanceCode;
    /**
    * 回退实例编码
    */
    @TableField("reverted_instance_code")
    private String revertedInstanceCode;
    /**
    * 审批定义编码
    */
    @TableField("approval_code")
    private String approvalCode;
    /**
    * 是否回退
    */
    @TableField("reverted")
    private Boolean reverted;
    /**
    * 审批实例编码
    */
    @TableField("instance_code")
    private String instanceCode;


    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String MAIN_ID = "main_id";

    public static final String APPROVAL_NAME = "approval_name";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String USER_ID = "user_id";

    public static final String OPEN_ID = "open_id";

    public static final String SERIAL_NUMBER = "serial_number";

    public static final String DEPARTMENT_ID = "department_id";

    public static final String STATUS = "status";

    public static final String UUID = "uuid";

    public static final String FORM = "form";

    public static final String TASK_LIST = "task_list";

    public static final String COMMENT_LIST = "comment_list";

    public static final String TIMELINE = "timeline";

    public static final String MODIFIED_INSTANCE_CODE = "modified_instance_code";

    public static final String REVERTED_INSTANCE_CODE = "reverted_instance_code";

    public static final String APPROVAL_CODE = "approval_code";

    public static final String REVERTED = "reverted";

    public static final String INSTANCE_CODE = "instance_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}