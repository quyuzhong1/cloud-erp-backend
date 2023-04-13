package com.erp.server.workflow;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

/**
 * @author Lambda
 * @Classname ErpServerWorkflowApplicationTest
 * @Description TODO
 * @Date 2023-04-12 11:00
 * @Created by yl
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWorkflowApplicationTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWorkflowApplicationTest {


    @Test
    public void createBpmn() throws IOException {
        BpmnModelInstance instance = Bpmn.createProcess()
                .startEvent()
                .userTask()
                .id("question")
                .exclusiveGateway()
                .name("Everything fine?")
                .condition("yes", "#{fine}")
                .serviceTask()
                .userTask()
                .endEvent()
                .moveToLastGateway()
                .condition("no", "#{!fine}")
                .userTask()
                .connectTo("question")
                .done();
        Bpmn.validateModel(instance);
        Resource resource = new ClassPathResource("diagrams");
        String path = resource.getFile().getPath();

        File file = new File(path, "test.bpmn");
        Bpmn.writeModelToFile(file, instance);

    }


  //  @Test
    public void createBomBpmn() throws IOException {
//        BpmnModelInstance instance = Bpmn.createProcess()
//                .startEvent().name("申请")
//                .userTask().camundaAssignee("productManagers").
//                        multiInstance()
//                .camundaCollection("")
//                .camundaElementVariable("")
//                .id("question")
//                .exclusiveGateway()
//                .name("Everything fine?")
//                .condition("yes", "#{fine}")
//                .serviceTask()
//                .userTask()
//                .endEvent()
//                .moveToLastGateway()
//                .condition("no", "#{!fine}")
//                .userTask()
//                .connectTo("question")
//                .done();
//        Bpmn.validateModel(instance);
//        Resource resource = new ClassPathResource("diagrams");
//        String   path=resource.getFile().getPath();
//
//        File file = new File(path, "test.bpmn");
//        Bpmn.writeModelToFile(file, instance);

    }


  //  @Test
    public StartEvent createStart() throws IOException {
        Process process = createProcess("Process_bom");
        StartEvent startEvent = createElement(process, "StartEvent_1","申请" ,StartEvent.class);
       return startEvent;
    }

    private Process createProcess(String id) {
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);
        // 创建流程元素
        Process process = createElement(definitions, id,"", Process.class);

        return process;

    }

    private <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id,String name, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        element.setAttributeValue("name",name,true);
        parentElement.addChildElement(element);
        return element;
    }
}
