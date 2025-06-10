package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 三方生成查询
 * </p>
 *
 * @author will
 * @since 2025-05-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("approve_task_info")
public class ApproveTaskInfoEntity extends BaseEntity<ApproveTaskInfoEntity> {

    /**
    * 流程来源,CfgProcessRuleTypeEnum
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 类型,ApproveTaskTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 第三方单据（飞书实例name）
    */
    @TableField("third_definniation_name")
    private String thirdDefinniationName;
    /**
    * 第三方单号（飞书实例id）
    */
    @TableField("third_instance_id")
    private String thirdInstanceId;
    /**
    * 第三方审批定义
    */
    @TableField("third_approval_code")
    private String thirdApprovalCode;
    /**
    * 数大臣单据名称，采购订单等
    */
    @TableField("bussiness_key")
    private String bussinessKey;
    /**
    * 数大臣单号，单据号
    */
    @TableField("bussiness_code")
    private String bussinessCode;
    /**
     * 数大臣单据id，采购订单id等
     */
    @TableField("bussiness_id")
    private String bussinessId;
    /**
     * 发生时间
     */
    @TableField("happen_time")
    private LocalDateTime happenTime;

    /**
     * 执行状态，ApproveTaskStatusEnum枚举
     */
    @TableField("status")
    private String status;

    /**
     * 失败原因
     */
    @TableField("reason")
    private String reason;


    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String TYPE = "type";

    public static final String THIRD_DEFINNIATION_NAME = "third_definniation_name";

    public static final String THIRD_INSTANCE_ID = "third_instance_id";

    public static final String THIRD_APPROVAL_CODE = "third_approval_code";

    public static final String BUSSINESS_KEY = "bussiness_key";

    public static final String BUSSINESS_CODE = "bussiness_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}