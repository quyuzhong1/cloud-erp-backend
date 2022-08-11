package com.cloud.erp.workflow.service.impl;

import com.cloud.erp.workflow.service.ProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Classname 流程定义相关服务
 * @Description TODO
 * @Date 2022-08-10 15:11
 * @Created by yl
 */
@Service
@Slf4j
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 发布流程  根据名字 和对应的bpmn
     *
     * @param name
     * @param resource
     * @return void
     * @author yl
     * @date 2022-08-10 15:14
     */
    @Override
    public void deployDefinitionByResource(String name, String resource) {
        Deployment deploy = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(resource)
                .deploy();

        log.info("流程id=="+deploy.getId());
        log.info("流程名=="+deploy.getName());

    }
}
