package com.cloud.erp.workflow;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class ErpWorkflowApplicationTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;


    @Autowired
    private HistoryService historyService;




    @Autowired
    private RepositoryService repositoryService;

    @Test
    void contextLoads() {
        //通过流程定义ID删除
        repositoryService.deleteProcessDefinitions().byIds("1111").delete();
    }





}
