package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Classname WorkflowBusinessEntity
 * @Description TODO
 * @Date 2023-01-30 15:08
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_business")
public class WorkflowBusinessEntity implements Serializable {

    /**
     * 表id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 业务流程的key
     */
    private String businessKey;

    /**
     * 流程定义的key
     */
    private String processDefinitionKey;

    /**
     * 在camunda 下画的流程图 在workflow resources 下的目录
     */
    private String bpmnName;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 业务属性
     */
    private String businessType;

    /**
     * 业务参数
     */
    private String param;

    /**
     * 平台
     */
    private String platform;
}
