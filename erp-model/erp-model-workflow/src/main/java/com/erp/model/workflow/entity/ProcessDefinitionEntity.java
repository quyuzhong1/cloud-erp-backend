package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
     * 是否已发布
     */
    @TableField("is_deploy")
    private Boolean isDeploy;

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

    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_VERSION = "process_version";

    public static final String BPMN_XML = "bpmn_xml";

    public static final String IS_DEPLOY = "is_deploy";

    public static final String REMARK = "remark";

    public static final String REVIEW_SETTING = "review_setting";

    public ProcessDefinitionEntity(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        super(dto.getId());
        this.processName = dto.getProcessName();
        this.bpmnXml = dto.getBpmnXml();
        this.remark = dto.getRemark();
        this.reviewSetting = dto.getReviewSetting();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
