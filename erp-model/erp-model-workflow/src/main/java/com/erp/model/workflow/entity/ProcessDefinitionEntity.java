package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.enums.DictBasicEnum;
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
    private DictBasicEnum reviewSetting;

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
     * 是否禁用，false否，true是
     */
    @TableField("disabled")
    private Boolean disabled;

    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_VERSION = "process_version";

    public static final String BPMN_XML = "bpmn_xml";

    public static final String FIELD_REMARK = "remark";

    public static final String REVIEW_SETTING = "review_setting";

    public static final String DEPLOYMENT_ID = "deployment_id";

    public static final String DEPLOY_TIME = "deploy_time";

    public static final String IS_DEPLOY = "is_deploy";


    public ProcessDefinitionEntity(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        super(dto.getId());
        this.processName = dto.getProcessName();
        this.bpmnXml = dto.getBpmnXml();
        this.remark = dto.getRemark();
        this.reviewSetting = dto.getReviewSetting();
    }

    public ProcessDefinitionEntity(ProcessDefinitionDTO.ProcessChangeDTO dto) {
        super(dto.getId());
        this.processName = dto.getProcessName();
        this.bpmnXml = dto.getBpmnXml();
        this.remark = dto.getRemark();
        this.reviewSetting = dto.getReviewSetting();
        this.processVersion = MathUtil.add(dto.getProcessVersion(),MathUtil.ONE);
    }

    public ProcessDefinitionEntity(ProcessDTO.DeployDTO dto, String deploymentId, Date deploymentTime, int version) {
        super.setId(dto.getProcessDefinitionId());
        this.deploymentId = deploymentId;
        this.deployTime = LocalDateUtil.date2LocalDateTime(deploymentTime);
        this.isDeploy = Boolean.TRUE;
        this.processVersion = version;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
