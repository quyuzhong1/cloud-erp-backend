package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import lombok.Getter;
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
@TableName("process_business")
public class ProcessBusinessEntity extends BaseEntity<ProcessBusinessEntity> {

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id ")
    private String processDefinitionId ;

    /**
     * 业务key
     */
    @TableField("business_key")
    private String businessKey;

    /**
     * 启动条件
     */
    @TableField("start_condition")
    private String startCondition;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String PROCESS_DEFINITION_ID  = "process_definition_id ";

    public static final String BUSINESS_KEY = "business_key";

    public static final String START_CONDITION = "start_condition";

    public static final String DISABLED = "disabled";

    public ProcessBusinessEntity(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        this.processDefinitionId  = dto.getId();
        this.businessKey = dto.getBusinessKey();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
