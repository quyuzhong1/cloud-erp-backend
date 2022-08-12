package com.cloud.erp.workflow.modules.workflow.service;

/**
 * @Classname 流程定义相关服务
 * @Description TODO
 * @Date 2022-08-10 15:10
 * @Created by yl
 */
public interface ProcessDefinitionService {

    void deployDefinitionByResource(String name,String resource);
}
