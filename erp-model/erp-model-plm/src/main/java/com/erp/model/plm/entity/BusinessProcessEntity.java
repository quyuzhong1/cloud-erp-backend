package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务流程
 * @TableName business_process
 */
@TableName("business_process")
@Data
public class BusinessProcessEntity extends BaseEntity implements Serializable {

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


    private static final long serialVersionUID = 1L;


}