package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BusinessProcessDTO
 * @Description TODO
 * @Date 2022-10-18 11:56
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BusinessProcessDTO implements Serializable {

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
     * 所需要的参数
     */
    private String param;
}
