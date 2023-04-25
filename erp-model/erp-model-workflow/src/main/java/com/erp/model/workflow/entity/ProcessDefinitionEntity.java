package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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
@TableName("process_definition")
@NoArgsConstructor
public class ProcessDefinitionEntity extends BaseEntity<ProcessDefinitionEntity> {

    /**
     * 流程名称
     */
    @TableField("process_name")
    private String processName;

    /**
     * 流程版本
     */
    @TableField("process_version")
    private Integer processVersion;

    /**
     * BPMN流程图
     */
    @TableField("bpmn_xml")
    private String bpmnXml;

    /**
     * 描述信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 审核人设置
     */
    @TableField("review_setting")
    private String reviewSetting;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 审核人
     */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
     * 审核人姓名
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 流程部署ID
     */
    @TableField("deployment_id")
    private String deploymentId;
    /**
     * 部署时间
     */
    @TableField("deploy_time")
    private LocalDateTime deployTime;

    /**
     * 是否已发布
     */
    @TableField("is_deploy")
    private Boolean isDeploy;

    /**
     * 审核意见
     */
    @TableField("comment")
    private String comment;


    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_VERSION = "process_version";

    public static final String BPMN_XML = "bpmn_xml";

    public static final String REMARK = "remark";

    public static final String REVIEW_SETTING = "review_setting";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String DEPLOYMENT_ID = "deployment_id";

    public static final String DEPLOY_TIME = "deploy_time";

    public static final String IS_DEPLOY = "is_deploy";

    public static final String COMMENT = "comment";


    public ProcessDefinitionEntity(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        super(dto.getId());
        this.processName = dto.getProcessName();
        this.bpmnXml = dto.getBpmnXml();
        this.remark = dto.getRemark();
        this.reviewSetting = dto.getReviewSetting();
    }

    public ProcessDefinitionEntity(String id, ApproveStatusEnum waitSubmit) {
        super(id);
        this.approveStatus = waitSubmit;
        this.isDeploy = Boolean.FALSE;
    }

    public ProcessDefinitionEntity(ProcessDTO.DeployDTO dto, String deploymentId, Date deploymentTime, String uid, String userName, int version) {
        super.setId(dto.getProcessDefinitionId());
        this.deploymentId = deploymentId;
        this.deployTime = LocalDateUtil.date2LocalDateTime(deploymentTime);
        this.approveUserId = uid;
        this.approveUserName = userName;
        this.approveTime = LocalDateTime.now();
        this.approveStatus = dto.getApproveCode();
        this.isDeploy = Boolean.TRUE;
        this.processVersion = version;
        this.comment = dto.getComment();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
