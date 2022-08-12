package com.cloud.erp.workflow;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ErpWorkflowApplicationTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;




    @Autowired
    private RepositoryService repositoryService;

    @Test
    void contextLoads() {
        repositoryService.deleteDeployment("e25f9e7c-1854-11ed-8445-50ebf621d770",true);
    }





}
